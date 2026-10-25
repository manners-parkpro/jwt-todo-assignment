"""PreToolUse(Bash) 훅: git commit·push 전에 되돌리기 어려운 실수를 막는다(D-19).

- commit: 명령에서 작성자 정보를 덮어쓰거나 저장소 로컬 user.email이 GitHub noreply 주소가 아니면 막는다.
          JDK 17로 ./gradlew test를 실행해 실패하면 막는다(테스트가 깨진 상태로 커밋하지 않는다).
- push:   강제 푸시를 막고(원격 기록 덮어쓰기는 사용자가 직접 한다), 올라갈 커밋의 작성자·커미터가
          모두 noreply 주소인지 확인한다.
회사 계정·도메인을 파일에 적지 않도록 허용 목록(noreply 접미사)으로 검사한다.
막을 때는 stdout JSON으로 permissionDecision "deny"와 이유를 내보낸다. 훅 설정은 로컬 전용 .claude/settings.local.json에 있다.
"""
import json
import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_DIR = Path(__file__).resolve().parent.parent.parent
NOREPLY_SUFFIX = "@users.noreply.github.com"
GIT_SUBCOMMAND = r"(?:^|[\s;&|(])git(?:\s+-[Cc]\s+\S+)*\s+{}\b"
COMMIT = re.compile(GIT_SUBCOMMAND.format("commit"))
PUSH = re.compile(GIT_SUBCOMMAND.format("push"))
SEGMENT_END = re.compile(r"&&|\|\||[;|\n]")
IDENTITY_OVERRIDE = re.compile(r"--author|user\.(?:email|name)|GIT_(?:AUTHOR|COMMITTER)_")
FORCE_PUSH = re.compile(r"\s(?:--force(?:-with-lease)?|-f)(?=[\s=]|$)|\s\+\S")
TEST_TIMEOUT_SECONDS = 280


def git(*args):
    return subprocess.run(["git", *args], cwd=PROJECT_DIR, capture_output=True, text=True).stdout.strip()


def deny(reason):
    print(json.dumps({"hookSpecificOutput": {
        "hookEventName": "PreToolUse",
        "permissionDecision": "deny",
        "permissionDecisionReason": reason,
    }}, ensure_ascii=False))
    sys.exit(0)


def segment(command, match):
    # 같은 Bash 호출의 다른 명령(예: rm -f)을 git 옵션으로 오인하지 않도록, git이 든 단순 명령 구간
    # (앞의 환경 변수 지정 포함)만 본다
    git_pos = command.index("git", match.start())
    start = max((m.end() for m in SEGMENT_END.finditer(command, 0, git_pos)), default=0)
    end = SEGMENT_END.search(command, git_pos)
    return command[start:end.start() if end else len(command)]


def check_commit(command):
    if IDENTITY_OVERRIDE.search(command):
        deny("커밋 작성자 정보는 저장소 로컬 git 설정으로만 정한다 — 명령에서 덮어쓰지 않는다(D-13).")
    if not git("config", "user.email").endswith(NOREPLY_SUFFIX):
        deny("저장소 로컬 user.email이 GitHub noreply 주소가 아니어서 커밋을 막는다(D-13).")
    java_home = subprocess.run(["/usr/libexec/java_home", "-v", "17"], capture_output=True, text=True)
    env = dict(os.environ, JAVA_HOME=java_home.stdout.strip()) if java_home.returncode == 0 else None
    try:
        result = subprocess.run(["./gradlew", "test", "--console=plain"], cwd=PROJECT_DIR, env=env,
                                capture_output=True, text=True, timeout=TEST_TIMEOUT_SECONDS)
    except subprocess.TimeoutExpired:
        deny(f"./gradlew test가 {TEST_TIMEOUT_SECONDS}초 안에 끝나지 않아 커밋을 막는다.")
    if result.returncode != 0:
        tail = "\n".join((result.stdout + result.stderr).splitlines()[-15:])
        deny(f"JDK 17 ./gradlew test가 실패해 커밋을 막는다 — 원인을 고친 뒤 다시 커밋한다.\n{tail}")


def check_push(command):
    if FORCE_PUSH.search(command):
        deny("강제 푸시는 원격 기록을 덮어쓰므로 사용자가 직접 실행한다(D-13).")
    upstream = git("rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}")
    revisions = f"{upstream}..HEAD" if upstream else "HEAD"
    emails = set(git("log", "--format=%ae%n%ce", revisions).split())
    if any(not email.endswith(NOREPLY_SUFFIX) for email in emails):
        deny("올라갈 커밋에 GitHub noreply가 아닌 작성자·커미터 주소가 있어 푸시를 막는다(D-13).")


def main() -> None:
    event = json.load(sys.stdin)
    command = event.get("tool_input", {}).get("command", "")
    commit = COMMIT.search(command)
    if commit:
        check_commit(segment(command, commit))
    push = PUSH.search(command)
    if push:
        check_push(segment(command, push))


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"guard-git 실행 실패: {error}", file=sys.stderr)
        sys.exit(1)
