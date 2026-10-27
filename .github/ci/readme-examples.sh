#!/usr/bin/env bash
# README "요청 예시 (curl)" 절의 bash 코드 블록을 적힌 그대로 이어서 실행하고,
# 응답 상태 코드가 README 본문에 적은 기대값(→ `201` 등)과 같은 순서로 나오는지 비교한다.
# 응답에는 토큰이 들어 있으므로 실행 출력은 임시 파일에만 두고, 공개 CI 로그에는 상태 코드만 남긴다.
# 사용: bash .github/ci/readme-examples.sh [bash|zsh] — 서버가 README 기본 주소(http://localhost:8080)에서 실행 중이어야 한다
set -euo pipefail

shell="${1:-bash}"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

awk '/^## 요청 예시/ { inside = 1; next } /^## / { inside = 0 } inside' README.md > "$work/section.md"
awk '/^```bash$/ { block = 1; next } /^```$/ { block = 0 } block' "$work/section.md" > "$work/examples.sh"
expected=$(awk '/^```/ { block = !block; next } !block' "$work/section.md" | grep -oE '→ `[0-9]{3}`' | grep -oE '[0-9]{3}' | tr '\n' ' ' || true)

# 아래 실행은 스크립트(비대화형)라 대화형 셸에 붙여 넣을 때만 깨지는 문장을 잡지 못한다. 그런 문장은 정적으로 막는다:
# 주석(macOS 기본 zsh는 대화형에서 #을 주석으로 보지 않는다), !(대화형 bash·zsh의 히스토리 확장), zsh 특수 변수 USERNAME 대입(무시된다)
if grep -nE '(^|[[:space:]])#|!|(^|[^A-Za-z0-9_])USERNAME=' "$work/examples.sh"; then
  echo "대화형 셸에 붙여 넣으면 깨지는 문장이 있다(docs.md README 규칙)" >&2
  exit 1
fi

# README의 username은 초 단위 시각으로 만들므로, 같은 초에 연달아 실행하면 겹쳐 가입이 409가 된다
sleep 1
"$shell" "$work/examples.sh" > "$work/output.txt" 2>&1 || true
actual=$(grep -oE '^HTTP/[0-9.]+ [0-9]{3}' "$work/output.txt" | grep -oE '[0-9]{3}$' | tr '\n' ' ' || true)

echo "셸: $shell ($("$shell" --version 2>&1 | head -n 1))"
echo "README 기대 상태: $expected"
echo "실제 응답 상태:   $actual"
# 스크립트로 실행했을 때 셸 오류(따옴표·글로브·변수 문제)가 나면 상태 코드가 같아도 실패로 본다. 토큰처럼 긴 값은 가린다
if grep -E 'command not found|no matches found|parse error|syntax error|unbound variable|bad substitution|read-only variable' "$work/output.txt" \
    | sed -E 's/[A-Za-z0-9_-]{32,}/<masked>/g'; then
  echo "셸 오류가 있다" >&2
  exit 1
fi
[ -n "$expected" ] && [ "$expected" = "$actual" ]
