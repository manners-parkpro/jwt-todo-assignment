---
paths:
  - "README.md"
  - "docs/**"
  - "prompts/**"
---

# 문서 규칙

## README.md (제출물 — 명세: 선택한 스택, 실행 방법, 테스트 실행 방법, API 목록 요약)
1. 선택한 스택 — "B. Spring Boot"와 버전(Java 17 이상·Spring Boot 3.5.16·Gradle Wrapper 9.8.2·Spring Data JPA·SQLite3·JWT(HS256)·JUnit 5)
2. 사전 요구사항 — JDK 17 이상(Gradle 별도 설치 불필요)
3. 실행 방법 — `./gradlew bootRun`. 테이블은 이 명령만으로 자동 생성된다(`ddl-auto: update`, `todo.db` 위치)고 적는다. `JWT_SECRET`은 선택(미설정 시 임의 키, 재시작하면 액세스 토큰 무효 → 재발급)이며 고정하는 방법을 macOS/Linux 명령으로 적는다.
4. 테스트 실행 방법 — `./gradlew test` (환경 변수 불필요)
5. API 목록 요약 — `.claude/rules/api.md` 표와 일치, Swagger UI 주소
6. 인증 방법 — 로그인 → `Authorization: Bearer <access_token>`, 액세스 토큰 만료 1시간 → `POST /users/refresh`로 재발급(리프레시 토큰 7일, 재발급 때마다 교체되고 이전 토큰은 무효)

- README에는 SQL·`sqlite3` 명령을 적지 않는다(테이블 생성은 실행·테스트 명령만으로 이뤄진다 — `persistence.md`).
- 명령은 macOS/Linux 기준으로 적고, Windows는 Gradle Wrapper 표준 명령(`.\gradlew.bat bootRun`, `.\gradlew.bat test`)만 함께 적는다. 이 PC(macOS)에서 실행할 수 없으므로 미실측임을 `docs/progress.md`에 남긴다(D-18).
- 그 밖의 명령은 실제로 실행해 확인한 것만 적고, 확인 근거를 `docs/progress.md`에 남긴다.
- 예시에는 실제 토큰·키 대신 `<access_token>` 같은 자리표시자를 쓴다.

## prompts/used_prompts.md (필수 제출물 — 없으면 실격)
- 명세: "사용한 주요 프롬프트와, 받은 결과를 어떻게 확인하거나 고쳤는지 간단히". 링크로 대체할 수 없고 파일 내용만으로 확인할 수 있어야 한다.
- 항목 하나 = 주요 AI 작업 하나, 짧게(절마다 1~4줄): ① 목적 ② 주요 프롬프트 원문(긴 첨부 문서는 요지만) ③ AI 제안 핵심 ④ 확인 방법(실행한 명령·확인한 문서) ⑤ 발견한 오류·개선점 ⑥ 반영 내용.
- 실제로 사용한 프롬프트와 실제로 수행한 검증만 쓴다. 비밀번호·키·토큰과 회사 관련 계정·이메일은 기록하지 않는다(원문의 계정명은 `<다른 계정>`으로 가린다). 상단 목차 표를 함께 갱신한다.
- 사용자 프롬프트 원문은 받은 턴 안에 기록한다. 원문 대조는 `.claude/prompt-history.jsonl`(훅 자동 기록, 로컬 전용)로 하며, 슬래시 명령과 하네스 알림(`<task-notification>`·`<agent-message>`)은 기록 대상이 아니다(D-16).

## docs/
- `assignment-spec.md`: 명세 원문 사본이다. 수정하지 않는다. 공개 저장소에서는 제외하고(`.gitignore`) 로컬과 제출 ZIP에만 둔다.
- `progress.md`: "현재 상태"와 "다음 작업"은 항상 최신으로 유지하고, 세션 로그는 맨 위에 추가한다. 약 150줄을 넘으면 오래된 로그를 요약한다.
- `decisions.md`: 이후 작업에 영향을 주는 결정만 기록한다. 번호는 증가(D-xx)시키고, 바뀐 결정은 삭제하지 않고 `대체됨(→D-yy)`으로 표시한다.
- `requirements-checklist.md`: 항목을 삭제하지 않는다. ✅는 실행 근거가 있을 때만 표시하고 근거를 적는다.
