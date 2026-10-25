<!-- 유지보수 메모(컨텍스트에 로드되지 않음): 이 파일은 매 세션 전체가 로드되므로 200줄 미만으로 유지한다(Claude Code 공식 권장).
     일부 영역에만 필요한 규칙 → .claude/rules/(paths로 자동 로드), 다단계 절차 → .claude/skills/, 진행 상태 → docs/.
     각 결정의 근거와 검증 출처는 docs/decisions.md에 있다.
     로드 확인: /context의 Memory files, 또는 로컬 .claude/instructions-log.jsonl(InstructionsLoaded 훅 기록, D-17). -->

# JWT TODO API — 과제 수행 지침

JWT 인증이 적용된 TODO REST API 채용 과제다. 평가 기준은 요구사항대로 동작하는가 · 코드를 읽기 쉬운가 · 테스트가 핵심 흐름을 검증하는가 · AI를 어떻게 활용하고 검증했는가다.
**우선순위: 과제 명세 > 이 문서 > `.claude/rules/`.** 충돌하면 명세를 따르고 `docs/decisions.md`에 기록한다. 명세 원문 사본은 `docs/assignment-spec.md`다(로컬 전용·공개 저장소 제외 — 없으면 체크리스트 상단의 출처에서 다시 수집).

## 기술 기준선 (근거 D-01·D-02·D-05·D-15)
- Java 17 타깃(`options.release = 17`, toolchain 없음) — 명세 "Java 17 이상"의 어떤 JDK에서도 빌드되게 한다. 검증은 JDK 17이 기본이고, 단계 완료·제출 전에 JDK 21로 교차 확인한다(채점 JDK 대비)
- Spring Boot 3.5.16 — 명세의 JUnit 5를 충족한다(4.x는 JUnit 6을 관리하므로 올리지 않는다)
- Gradle Wrapper 9.8.2 유지 — JDK 17~26에서 실행된다(8.14는 JDK 25 이상에서 실행 불가)
- 인증: `spring-boot-starter-oauth2-resource-server`(Nimbus) — 액세스 토큰 JWT(HS256, 1시간) + 리프레시 토큰(불투명·DB 저장·7일·회전, 8단계 확장). jjwt나 직접 만든 JWT 필터는 쓰지 않는다
- Lombok·springdoc(2.8.17)은 유지하되 사용 범위는 rules를 따른다

## 명령어
```bash
# 이 PC의 기본 JDK는 21이고 셸 환경 변수는 Bash 호출마다 초기화된다 → JDK 지정은 명령마다 접두로 붙인다
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew test                    # 기본 검증 — 필수 테스트(환경 변수 불필요)
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew test --tests '*Todo*'   # 일부 테스트만 실행
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew clean build             # 단계 완료·제출 전 교차 확인
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew --version               # 사용 JDK는 Launcher JVM으로 확인(java -version은 PATH의 21)
./gradlew bootRun                                                            # 로컬 실행 (JWT_SECRET 미설정 시 임의 키 생성)
JWT_SECRET="$(openssl rand -base64 32)" ./gradlew bootRun                    # 서명 키를 고정해 실행
sqlite3 todo.db '.schema'                                                    # 테이블·유니크·인덱스 확인 (개발 측 검증용 — README에는 쓰지 않음)
```

## 작업 원칙
1. 수정 전에 관련 파일과 테스트를 읽는다. 추측으로 코드·설정을 바꾸지 않고, 기존 변경 사항을 덮어쓰거나 이미 된 구현을 반복하지 않는다. 기존 설정은 명세와 충돌하지 않는 한 존중한다.
2. 요구사항의 기준은 `docs/requirements-checklist.md`(명세 기반)다. 명세에 없는 것을 필수로 만들지 않고, 설계상 선택은 근거와 함께 `docs/decisions.md`에 남긴다.
3. **단계 게이트**: 설계가 승인되기 전에는 구현 파일(`build.gradle`, `src/`, `README.md`, `.gitignore` 등)을 만들거나 고치지 않는다. 승인 후에는 `docs/progress.md`의 다음 작업을 한 단계씩 진행한다 — 계획 → 구현 → 검증(실행) → 독립 리뷰(구현 단계는 `step-reviewer` 서브에이전트 — 정확성·요구사항 지적만 반영) → 결과 보고 → 사용자 확인 후 다음 단계. 여러 기능을 한 번에 만들지 않는다. 현재 단계는 `docs/progress.md`에 있다.
4. "통과·완료"는 실제로 실행한 명령과 그 결과로만 주장한다. 실행하지 못한 것은 이유와 함께 미검증으로 보고한다.
5. 테스트를 통과시키려고 기대값이나 검증을 약화·삭제하거나 `@Disabled` 처리하지 않는다. 실패하면 원인을 고친다.
6. 의미 없는 인터페이스·추상화·라이브러리·중복 코드를 만들지 않는다(YAGNI). 포트는 `.claude/rules/architecture.md`의 정책만 따른다.
7. 비밀번호·JWT 키·실제 토큰을 코드·로그·문서·프롬프트 기록에 남기지 않는다.
8. 새 파일을 만들기 전에 해당 영역의 `.claude/rules/` 파일을 먼저 읽는다. 경로 규칙은 일치하는 파일을 Read·Edit·Write 도구로 다룰 때 로드된다 — 새 파일은 쓴 뒤에야 로드되고, 셸로 편집하면 로드되지 않을 수 있으며, 압축·새 세션 뒤에는 다시 로드될 때까지 빠져 있다. 그래서 규칙 대상 파일은 Read·Edit·Write 도구로 다룬다.
9. 보안 문제, YAGNI, 더 나은 설계안을 발견하면 즉시 보고한다.
10. 커밋은 단계 검증 뒤 `type: 한국어 요약`(feat·fix·test·docs·build·chore) 형식으로 하고 푸시한다. 작성자는 저장소 로컬 설정(GitHub noreply)만 쓰고, 강제 푸시 같은 원격 기록 덮어쓰기는 사용자가 직접 한다 — `guard-git.py` 훅이 작성자·테스트 통과·강제 푸시를 검사한다(D-13·D-19).

**IMPORTANT: `prompts/used_prompts.md`가 없거나 비어 있으면 실격이다.** 주요 AI 작업(설계, 기능 단위 구현, 디버깅, 문서화)을 마칠 때마다 실제 프롬프트와 결과를 어떻게 확인·수정했는지 간단히 추가한다. 사용자 프롬프트 원문은 받은 턴 안에 기록한다 — 컨텍스트가 초기화·압축되면 대화 속 원문은 사라진다(원문 백업은 훅이 `.claude/prompt-history.jsonl`에 자동으로 남긴다). 하지 않은 일은 쓰지 않는다.

## 세션 루틴
**시작**(새 세션·압축 직후 포함) — 기록과 실제 코드 상태를 대조한 뒤 작업한다. 기록만 믿고 구현됐다고 가정하지 않는다.
1. `docs/progress.md`(현재 상태·다음 작업)와 `docs/requirements-checklist.md`를 읽는다. `docs/decisions.md`는 필요한 결정만 찾아본다.
2. `git status`와 `git log --oneline -10`으로 변경 사항을 확인한다.
3. `.claude/prompt-history.jsonl` 끝부분과 `prompts/used_prompts.md`를 대조해, 빠진 주요 사용자 프롬프트가 있으면 먼저 기록한다(슬래시 명령·하네스 알림은 제외).
4. 구현을 재개하기 전에 JDK 17로 `./gradlew test`를 실행해 기준선을 확인한다. 실패하면 다른 작업보다 먼저 원인을 찾아 보고한다(구현 파일 수정은 단계 게이트를 따른다).

**종료** — 주요 작업마다 아래를 갱신한다.
- `docs/progress.md`: 맨 위에 세션 로그(완료 작업·수정 파일·실행 명령·실제 결과·실패 원인·다음 작업), "현재 상태/다음 작업" 최신화
- `docs/requirements-checklist.md`: 상태 갱신 (✅는 실행 근거가 있을 때만)
- `docs/decisions.md`: 이후 작업에 영향을 주는 결정만 추가
- `prompts/used_prompts.md`: 항목 추가

## 완료 기준
필수 API 8개 동작 · 필수 테스트 4종 통과 · README 명령만으로 실행/테스트 재현 · `prompts/used_prompts.md` 작성.
**확장 기능(Refresh Token·선택 사항) 때문에 필수 API와 필수 테스트 4종이 제약·변경·누락되면 안 된다** — 필수 테스트는 확장 기능 없이도 통과해야 한다.
코드를 작성했다는 이유만으로 완료 처리하지 않는다. 최종 점검과 제출 ZIP 생성은 `/submission-check <이름>`으로 한다.

## 문서 지도
| 경로 | 내용 | 로드 시점 |
|---|---|---|
| `.claude/rules/build.md` | 빌드 기준선·의존성·버전 오버라이드 | 빌드 파일 접근 시 |
| `.claude/rules/architecture.md` | 헥사고날·DDD 구조, 의존 방향, 포트 정책 | `src/main/java/**` 접근 시 |
| `.claude/rules/security.md` | 인증 구성·JWT·비밀번호·소유권·사용 금지 기능 | `src/**` 접근 시 |
| `.claude/rules/persistence.md` | SQLite·JPA 설정과 DDL 함정 | 리소스·영속성 어댑터 접근 시 |
| `.claude/rules/api.md` | API 계약·상태 코드·오류 형식·API 문서 | 웹 어댑터·`global/**`·테스트·README 접근 시 |
| `.claude/rules/testing.md` | 테스트 구성·격리·검증 원칙 | `src/test/**` 접근 시 |
| `.claude/rules/docs.md` | README·프롬프트 기록·진행 문서 작성 규칙 | `README.md`·`docs/**`·`prompts/**` 접근 시 |
| `.claude/skills/submission-check/` | 최종 검증·제출 ZIP 절차 | `/submission-check` 실행 시 |
| `.claude/agents/step-reviewer.md` | 단계 변경분 독립 리뷰(읽기 전용 서브에이전트) | 구현 단계 완료 보고 전 |
| `.claude/hooks/` | 로컬 훅 — `log-prompt.py`(프롬프트 원문 → `prompt-history.jsonl`), `log-instructions.py`(지침 로드 → `instructions-log.jsonl`), `guard-git.py`(커밋·푸시 전 작성자·테스트·강제 푸시 검사). 설정은 로컬 `.claude/settings.local.json` | 프롬프트 제출·지침 로드·Bash 실행 시 |
| `.claude/*.jsonl` | 위 훅의 기록(로컬 전용 — 공개 저장소·ZIP 제외) | 기록 대조·로드 확인 시 |

## 컨텍스트 압축 시 보존할 것
진행 중인 체크리스트 ID, 수정한 파일 목록, 마지막 테스트 명령과 결과(실패 메시지 포함), 미해결 결정 사항, `prompts/used_prompts.md`에 아직 기록하지 않은 사용자 프롬프트 원문.
