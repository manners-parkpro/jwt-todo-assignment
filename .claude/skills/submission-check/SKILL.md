---
name: submission-check
description: 과제 최종 제출 전 검증(클린 빌드·JDK 17 테스트·실행 스모크·스키마·정적 점검·문서)과 제출 ZIP 생성, 최종 보고. 사용자가 직접 실행한다.
argument-hint: "<이름>"
disable-model-invocation: true
---

# 최종 제출 점검 — 제출자: $ARGUMENTS

순서대로 수행하고 단계마다 실제 명령과 결과를 기록한다. 실패하면 멈추고 원인을 보고한다(우회·생략 금지).

1. **체크리스트**: `docs/requirements-checklist.md`에서 ⬜·🟨·❌ 항목을 모두 나열한다.
2. **빌드·테스트**: `./gradlew clean build`(JDK 21)와 `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test`(명세 최소 JDK) → 통과·실패 수 확인(`build/test-results/test/*.xml`).
3. **실행 스모크** (README 명령 그대로, `JWT_SECRET` 없이, `todo.db`를 지운 클린 상태에서):
   - `./gradlew bootRun`을 백그라운드로 실행하고 기동 로그(임의 키 WARN 포함)를 확인한다.
   - curl로 상태 코드 확인: 가입 201 → 로그인 200(`access_token`·`refresh_token` 존재) → `/users/me` 200 → TODO 생성 201 → 목록 200 → 수정 200 → 삭제 204 → 토큰 없이 `/todos` 401 → 없는 ID 404 → 재발급 200(새 토큰 쌍) → 사용한 리프레시 토큰 재사용 401 → `/v3/api-docs` 200(`access_token`·`refresh_token` 표기 확인).
     ```bash
     B=http://localhost:8080; U="smoke$RANDOM"; P='password1234'
     curl -s -o /dev/null -w '%{http_code}\n' -X POST $B/users/signup -H 'Content-Type: application/json' -d "{\"username\":\"$U\",\"password\":\"$P\"}"
     T=$(curl -s -X POST $B/users/login -H 'Content-Type: application/json' -d "{\"username\":\"$U\",\"password\":\"$P\"}" | python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])')
     curl -s -o /dev/null -w '%{http_code}\n' $B/users/me -H "Authorization: Bearer $T"
     curl -s -o /dev/null -w '%{http_code}\n' $B/todos
     ```
   - `sqlite3 todo.db '.schema'` → `users`(username `unique`), `todos`(`user_id` 인덱스), `refresh_tokens`(`token_hash` `unique`).
   - 종료 후 남은 프로세스가 없는지 확인한다. 토큰 값은 출력·기록하지 않는다.
4. **정적 점검**:
   - 도메인 순수성: `grep -rnE 'import (org\.springframework|jakarta\.persistence|com\.fasterxml)' src/main/java/com/assignment/*/domain` → 출력 없음
   - 고정 키: `grep -rniE 'secret' src/main/resources src/test/resources` → `${JWT_SECRET:}` 참조 외 없음
   - 사용 금지 기능: `grep -rnE 'UnboundIdContainer|AesBytesEncryptor|SpelExpressionParser|nativeQuery|@NativeQuery' src/main` → 출력 없음
   - 테이블 생성 경로 단일화: `ls src/main/resources` → `schema.sql`·`data.sql` 없음, `grep -niE 'sqlite3|create table|insert into' README.md` → 출력 없음
   - 커밋 작성자: `git log --format='%ae%n%ce' | sort -u` → GitHub noreply 주소만(회사 도메인 없음)
   - 버전: `build.gradle`의 Boot 3.5.x·`options.release = 17`·`tomcat.version`, `gradle-wrapper.properties`의 9.8.2(폴백했다면 8.14.6과 README 안내)
5. **문서**: README 필수 섹션(`.claude/rules/docs.md`)이 있고 명령이 실측과 일치하는지, `prompts/used_prompts.md`가 최신 작업까지 기록됐는지 확인한다.
6. **ZIP** — `.git/` 포함 여부를 먼저 사용자에게 확인한다(아래 명령은 제외가 기본). 프로젝트 상위 폴더에 최상위 폴더째 압축한다.
   ```bash
   P="$(basename "$PWD")"; N="$ARGUMENTS"
   cd .. && zip -rq "${N}_springboot.zip" "$P" \
     -x "$P/build/*" "$P/target/*" "$P/.gradle/*" "$P/out/*" "$P/bin/*" "$P/.idea/*" "$P/.vscode/*" "$P/.git/*" \
        "*.db" "*.db-*" "*.iml" "*.DS_Store" "$P/.env*" "$P/.claude/settings.local.json" "$P/.claude/prompt-history.jsonl" "$P/HELP.md"
   unzip -l "${N}_springboot.zip" | grep -E '/(build|target|\.gradle)/|\.db' && echo '!! 제외 실패'
   unzip -l "${N}_springboot.zip" | grep -E 'gradlew$|gradle-wrapper\.jar|README\.md|prompts/used_prompts\.md|docs/assignment-spec\.md|src/(main|test)/' | head
   ```
7. **기록**: 체크리스트(SUB-xx 포함)·`docs/progress.md`·`prompts/used_prompts.md`를 갱신한다.
8. **최종 보고** — 검증하지 못한 항목은 이유와 함께 밝힌다.
   1) 구현한 API와 주요 기능 2) 아키텍처와 주요 설계 결정 3) 주요 변경 파일 4) 테스트·빌드 명령과 실제 결과 5) SQLite 테이블 자동 생성 확인 여부 6) README·`prompts/used_prompts.md` 작성 여부 7) ZIP 구성·제외 항목 확인 결과 8) 남은 문제·미검증 사항
