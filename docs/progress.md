# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-26
- **단계: 11단계(선택 기능)·10단계(추가 테스트) 완료 → 사용자 확인 후 12단계(README).** 사용자 지시(#14)로 11단계를 10단계보다 먼저 했다. 설계 승인은 2026-10-25(used_prompts #2).
- **제출 전 남은 실격 위험: `README.md`가 아직 없다(12단계).** README에 적을 명령(`./gradlew test`·`./gradlew bootRun`, Windows `.\gradlew.bat …`)은 CI로 3개 OS × JDK 17·21 실측을 마쳤다.
- **제출 전 남은 실격 위험: `README.md`가 아직 없다(12단계).** 지금 제출하면 실격 조건 1("README에 적힌 명령어로 실행되지 않는 경우")에 해당한다. README에 적을 명령은 CI로 3개 OS × JDK 17·21 실측을 마쳤다.
- 빌드·테스트: JDK 17·21 `clean build` 45건 통과(S16 — 필수 TEST-01~04, 확장 07·08·09, 선택 X1·X2·X3·X4, API-10, OPT-01~03). 2회 연속·무작위 순서 실행 통과. CI(GitHub Actions) Windows·Linux·macOS × JDK 17·21 — 10단계 커밋 `db98c0d`까지 모두 성공.
- 사용자 결정(S12·S13): Lombok 1.18.48(JDK 27 실격 위험 해결), 정기 교차 확인 JDK는 17·21, `memory-bank/`는 로컬에 두고 Git·ZIP에서만 제외, TTL은 yml 유지, Windows·Linux는 GitHub Actions로 실측. 서명 키 환경 변수는 `TODO_JWT_SECRET`(S13 — 흔한 이름 충돌로 기동 실패하는 위험 보강).
- 코드 구조(S10, D-20): 유스케이스는 Result를 반환하고 웹 어댑터가 Response로 변환한다. 포트·어댑터 이름은 사용자가 지정한 참고 프로젝트 규칙(CommandPort·QueryPort, RepositoryAdapter)을 따른다.
- 원격: 저장소 로컬 자격 증명 헬퍼로 푸시한다. 원격 커밋 작성자·커미터는 모두 noreply다.
- 지침·기록(S7): 압축 뒤 CLAUDE.md 전체 주입 확인. 로컬 훅 3개 — 프롬프트 원문 백업(하네스 메시지 제외), 지침 로드 기록, 커밋·푸시 검사(작성자·테스트·강제 푸시).

## 다음 작업 (한 단계씩 — 계획·구현·검증·독립 리뷰·보고 후 사용자 확인)
1. [x] 사용자 확인 사항 처리 — 명세 확보, D-01·D-02 결정, `memory-bank/` 삭제(백업), `git init`
2. [x] 저장소 정리·원격 연결 — `.gitignore` 보강, Initializr 잔여물 삭제, origin 연결
3. [x] 빌드 기준선 교정 — `build.gradle` Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17·Lombok main만 (검증: S4)
4. [x] 설정과 테스트 베이스 — `application.yaml`(SQLite·JPA), `application-test.yaml`, `IntegrationTestSupport` (검증: S6). `auth.*`는 사용하는 코드와 함께 5단계로 조정
5. [x] 보안 골격: `auth.jwt-secret`·SecurityConfig·서명 키와 JWT 디코더(임의 키 대체)·공개 인증 경로용 `BearerTokenResolver`·EntryPoint·GlobalExceptionHandler → TEST-03 — SEC-02·04·07·08·12, ERR-01 (검증: S8)
6. [x] User·Auth 필수 범위: `JwtEncoder`(HS256 헤더 명시)·`TokenIssuer`·비밀번호 해시 어댑터·`@CurrentUserId`, 가입·내 정보·로그인(**`access_token`만 발급**) → TEST-01 — API-01~03, SEC-01·03·06 (검증: S9)
7. [x] Todo 기능 → TEST-02·04(GET·PUT·DELETE) + TEST-X2(본인 TODO만 — 명세 필수 동작이라 S10에 10단계에서 앞당김), 클린 상태 `bootRun`으로 `users`·`todos` `.schema` 중간 확인, 동시 요청으로 커넥션 풀 1개 동작 확인(R-3) — API-04~09, ERR-02·03, DB-02·03. 사용자 지시로 Result → Response 변환·네이밍 정리(D-20)를 user 기능까지 적용 (검증: S10)
8. [x] 재발급 확장: 로그인 응답에 `refresh_token` 추가, `refresh_tokens`, `POST /users/refresh`(회전·재사용 탐지, 공개 엔드포인트 매처에 추가) → TEST-07·08 + TEST-09(만료, S12 추가) + **TEST-01~04 회귀**, `refresh_tokens` `.schema` 확인 — API-11, SEC-11, DB-06 (검증: S12)
9. [x] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10 (검증: S14)
10. [x] 선택 테스트 TEST-X1·X3·X4(X2는 7단계), 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06, ERR-04 (검증: S16, 11단계 뒤에 진행)
11. [x] 선택 기능 OPT-01~03 구현 — D-21, OPT-01 → 02 → 03 순서로 기능마다 `[OPT-0x]` 테스트·TEST-01~04 회귀·`ApiDocsTest` 목록 갱신 (검증: S15, 사용자 지시로 10단계보다 먼저)
12. [ ] README 작성과 명령 실검증(최종 API 기준, macOS/Linux 실측 + Windows 표준 Wrapper 명령 — CI로 3개 OS × JDK 17·21 확인, SQL 명령 없음, 8080 포트 사용 중일 때의 실행 방법 실측), 클린 상태에서 README 명령으로 테이블 생성 최종 확인 — DOC-01~05, TECH-06·07, D-18
13. [ ] `/submission-check <이름>` — SUB-01~06, SEC-09·10, DOC-06~08

## 환경 메모
- JDK: 이 PC 기본은 Corretto 21.0.10. 기본 검증은 OpenJDK 17.0.18이며, 셸 환경 변수가 Bash 호출마다 초기화되므로 명령마다 `JAVA_HOME=$(/usr/libexec/java_home -v 17)`을 붙인다. `java -version`은 PATH 앞쪽의 Corretto 21을 가리키므로 사용 JDK는 `./gradlew --version`의 Launcher JVM으로 확인한다. 단계 완료 시 21로 교차 확인. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip·jq 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개). 저장소 로컬 작성자 = `Colin <43167000+manners-parkpro@users.noreply.github.com>`. `docs/assignment-spec.md`, `.claude/settings.local.json`, `.claude/prompt-history.jsonl`은 `.gitignore`로 제외. 푸시 인증은 저장소 로컬 `credential.https://github.com.helper`가 `gh auth token --user manners-parkpro`를 사용(gh 활성 계정·전역 설정은 그대로)
- 로컬 훅(설정은 `.claude/settings.local.json`): `log-prompt.py`(UserPromptSubmit → `.claude/prompt-history.jsonl`, `<task-notification>`·`<agent-message` 제외 — 그 전에 기록된 4건은 대조할 때 건너뜀), `log-instructions.py`(InstructionsLoaded → `.claude/instructions-log.jsonl`), `guard-git.py`(PreToolUse Bash — 커밋 때 JDK 17 테스트로 약 10초). 세션 중에 만든 에이전트·지침 파일은 그 세션에서 로드되지 않을 수 있다 — `step-reviewer`를 고를 수 없으면 같은 파일 내용으로 general-purpose 에이전트를 쓴다

## 세션 로그 (최신이 위)

### 2026-10-26 · S16 10단계 추가 테스트(TEST-X1·X3·X4), 반복·순서 독립 확인
- 작성: `InvalidTokenTest`(TEST-X1 4건 — 다른 키 서명·sub 변조·alg: none·1초 전 만료), `DuplicateSignupTest`(TEST-X3), `ValidationErrorTest`(TEST-X4 6건 — 가입 4종 파라미터화·TODO 4종·읽을 수 없는 본문·숫자 아닌 ID). 운영 코드 변경 없음. 커밋 `db98c0d`, 리뷰 반영 `1e1e081`
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → 45건 통과(첫 실행). JDK 17 `cleanTest test` 2회 연속 45/45, 무작위 클래스·메서드 순서(시드 20261026·7) 45/45 두 번 — 결과 XML 시각으로 순서가 실제로 섞였음을 확인, 임시 설정 파일은 삭제
  - JDK 17·21 `clean build` → BUILD SUCCESSFUL, 45건. 테스트 DB 표 0개(create-drop), `todo.db` 미생성
  - 변이 4종(되돌린 뒤 `cmp`): 서명을 검증하지 않는 디코더 → X1 4건 모두 실패, 시계 오차 0 설정 제거 → 1초 전 만료 건만 실패(TEST-07은 1시간 전 만료라 이 회귀를 못 잡음), 가입 중복 확인 제거 → X3 실패, TODO 생성 `@Valid` 제거 → X4 실패. 리뷰 반영 뒤 재실행해도 같음
  - 401 거부 사유 실측: 만료 "Jwt expired at …", 다른 키·변조 "Signed JWT rejected: Invalid signature", alg: none "Unsupported algorithm of none"(디코더의 서명 없는 토큰 거부 경로) — 이 문구를 테스트에 단언
  - CI(`db98c0d`, run 40298369548) 6개 조합 성공·스모크 6/6·경고 0
- 독립 리뷰(`step-reviewer`): 정확성·보안·명세 결함 없음. 낮음 2건 반영 — ① TEST-X4는 필드가 모두 한 단어라 JSON 이름 변환을 구별하지 못함 → 체크리스트 근거를 "요청 필드 이름"으로 좁히고 변환 근거는 OPT-02로 적음 ② TEST-X1 변조 건·TEST-X3이 refresh_token을 읽는 도우미를 써서 확장에 묶임 → access_token 도우미·직접 가입 요청으로 바꿈. 참고 사항(거부 사유 미단언)도 반영
- 다음: 사용자 확인 후 12단계 README(최종 API 기준, 명령 실검증)

### 2026-10-26 · S15 11단계 선택 기능(OPT-01~03) 구현·실측 — 사용자 지시 #14
- 구현 전 재검토(보고 후 진행): D-21을 바꿀 더 나은 방안 없음.
  - 하드 삭제 + AUTOINCREMENT는 방언에서 불가, 요청마다 사용자 상태 조회는 필수 인증 경로 변경, username 해제는 기존 열 NOT NULL을 SQLite가 바꿀 수 없어 기각.
  - 로그인 타이밍 차이는 가입 API가 409로 존재를 알려 주므로 바꾸지 않음.
  - 구현 세부 4가지: 새 비밀번호 공백 거부, 미사용 조회 메서드 정리, 테스트 도우미는 추가만, 기존 DB 열 추가 실측.
- 구현·커밋(기능마다 테스트·회귀·실측 후):
  - OPT-01 검색 `21bd2ba`, OPT-02 수정 `0336d16`, OPT-03 탈퇴 `b20471c`.
  - 리뷰 반영: `f923d77`(요청 DTO 이름 `SearchTodoRequest`), `6b5bec6`(새 비밀번호 72바이트 테스트).
- 실행 명령과 결과:
  - JDK 17 `./gradlew test`: OPT-01 뒤 23건, OPT-02 뒤 29건, OPT-03 뒤 34건 통과. JDK 21 `clean build`도 기능마다 같은 건수 통과. CI 3개 OS × JDK 17·21은 기능 커밋 3개 모두 성공(스모크 6/6, 경고 0).
  - 변이 17종(되돌린 뒤 `cmp`): 16종 검출. `IgnoreCase` 제거는 통과 — SQLite `LIKE`가 원래 ASCII 대소문자를 구분하지 않음(D-21).
  - bootRun 실측(스크래치 `opt01-smoke.sh`·`opt02-smoke.sh`·`opt03-smoke.sh`·`rollback-check.sh`):
    - 검색 SQL에 `escape '\'`, 다른 사용자 TODO 제외, `%`·`_` 문자 그대로.
    - 수정 뒤 DB 해시 교체·옛 리프레시 토큰 401, 현재 비밀번호 틀림 400이 중복 409보다 먼저.
    - 탈퇴 뒤 로그인·재발급 401·`/users/me` 404·TODO 0행·같은 username 409. 마지막 가입자(id 2)가 탈퇴한 뒤 새 가입자는 id 3, 탈퇴자 토큰은 404.
    - OPT-03 이전 버전이 만든 `todo.db`에 새 버전 실행 → `alter table users add column withdrawn boolean default false not null`, 기존 계정 로그인·TODO 조회·탈퇴 정상.
    - 리스너가 실패하면 탈퇴 요청 500(일반 본문)이고 `withdrawn`·리프레시 토큰·TODO 모두 롤백.
    - 로그에 ERROR·토큰·비밀번호 0건.
  - 정적 점검: 도메인 프레임워크 import 0, web → domain 0, application의 Spring 웹·이벤트·JPA import 0, user ↔ todo 직접 import 0/0. 필수 테스트 파일 4개 변경 0건, 테스트 도우미 삭제 줄 0.
- 실패와 원인(끝까지 추적):
  - ① OPT-02 첫 실행 `compileTestJava` 실패 — `ApiDocsTest`의 `Map.of`는 키·값 10쌍까지만 받는데 API가 11개가 됐다 → `Map.ofEntries`로 바꿔 해결(탈퇴 추가로 12개).
  - ② 리뷰 반영 커밋 명령 실패 — `git mv`가 이미 스테이징한 옛 경로를 다시 `git add`해 pathspec 오류, 이어서 메시지 파일 생성 단계가 실행되지 않음 → 스테이징 확인 후 다시 커밋.
  - ③ 실측 스크립트 표시 오류 — `grep -o '[^\r]*'`가 "백슬래시나 r이 아닌 문자"로 해석돼 `withdrawn`의 r에서 잘림. 실제 ALTER 문은 로그 원문으로 확인.
- 독립 리뷰(`step-reviewer`): 기능·보안 결함 없음. 지적 1건(요청 DTO 이름) 반영. 참고 사항도 반영: 새 비밀번호 72바이트 테스트 추가, 탈퇴 뒤 남은 토큰으로 만든 TODO가 남는 한계를 D-21·security.md에 명시, 리뷰어가 확인하지 못한 기존 DB 열 추가·리스너 실패 롤백은 실측.
- 다음: 10단계(TEST-X1·X3·X4, 2회 연속 실행, `./gradlew build`)

### 2026-10-26 · S14 9단계 Swagger 문서(API-10), 사용자 지시 #13(제출 ZIP 실측·선택 사항 설계·평가 기준·실격 조건 점검)
- CI 재실행(run 40286963395, 커밋 `45ec1bb`): 6개 조합 성공, 스모크 6/6 통과, Windows 작업의 캐시 저장 경고 사라짐(Windows JDK 17 캐시 저장 성공, 빌드 로그의 `##[warning]`·`##[error]` 0건)
- 설정 전 문서(`api-docs-before.json`): 제목 "OpenAPI definition" v0, 보안 스키마 없음, 태그 `*-controller`, `POST /todos` 200, 응답 형식 `*/*`. 기동 로그에 springdoc "enabled by default" WARN 2건 — springdoc 2.8.17 소스에서 속성이 바인딩되지 않을 때 남기는 경고임을 확인
- 구현: `global/config/OpenApiConfig`(`@OpenAPIDefinition` 제목·설명·태그 순서, `@SecurityScheme` `bearerAuth` HTTP bearer JWT, 이름 상수), 컨트롤러 3개에 `@Tag`·`@Operation(summary)`, 보호 API에만 `@SecurityRequirement`(TodoController 클래스·`/users/me`), `POST /todos` `@ApiResponse(201)`, `application.yaml` springdoc 명시 설정 2개 + `default-produces-media-type`, 테스트 `ApiDocsTest`(API-10, 2건)
- 실행 명령과 결과:
  - bootRun + curl(`apidocs9.sh`): `/v3/api-docs` 200, `/swagger-ui.html` 302 → `/swagger-ui/index.html` 200, springdoc WARN 0건(남은 WARN은 의도한 서명 키 경고 1건), ERROR 0건. 문서: 보호 API 6개만 `security: bearerAuth`, `POST /todos` 201(`TodoResponse`), 응답 형식 `application/json`, 태그 사용자 → TODO
  - JDK 17 `./gradlew test` → 19건 통과. 변이 3종(되돌린 뒤 `cmp` 일치): `/users/me`의 `@SecurityRequirement` 제거 → 보안 표시 테스트 실패, `@ApiResponse(201)` 제거 → 상태 코드 테스트 실패, 리뷰 반영 뒤 `/users/me`가 없는 스키마 이름(`"bearer"`)을 가리키게 함 → 보안 표시 테스트 실패(반영 전 테스트는 `security` 존재만 봐서 통과했을 경우)
  - 헤드리스 Chrome 155 + DevTools Protocol(Node 내장 WebSocket, 스크래치 `swagger-ui-e2e.mjs`): Swagger UI 제목·태그 순서·자물쇠 6개 확인 → Authorize 대화상자에 로그인 토큰 입력 → `GET /users/me` 200(curl에 Bearer 헤더), `POST /todos` 201 + `location: /todos/{id}`, 승인 상태에서도 `POST /users/login`은 Bearer 헤더 없이 200. 실행 로그에 ERROR 0건, 비밀번호·토큰 0건
  - JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=19 failures=0, `todo.db` 없음. 정적 점검(도메인 순수성·web → domain·application의 프레임워크 import) 위반 0건, springdoc 어노테이션은 웹 어댑터와 `global/config`에만 있음
- 독립 리뷰(`step-reviewer`): 막을 문제 없음 — 실제 API 동작 불변(`default-produces-media-type`은 springdoc 문서 생성에만 쓰임, `application/problem+json` 단언 테스트 통과), TEST-01~04 파일 diff 없음, 테스트 19건 재현. 지적 4건 반영: ① `@Info` 3번 "로그인 응답의 refresh_token" → "가장 최근에 받은 refresh_token"(회전 때문에 두 번째 재발급부터 401·전체 폐기로 이어지는 안내였다) ② used_prompts #12 완료 기록 ③ `ApiDocsTest`가 `security` 존재만 보던 것을 `[{bearerAuth: []}]` 값 비교로 강화 ④ summary를 명세 문구로("내 목록 조회", "로그인, access_token 반환"). 반영 뒤 JDK 17·21 `clean build` 19건 통과, 기동 상태 문서에서 고친 문구 확인
- 실수·정정: 브라우저 확장이 연결되어 있지 않아 헤드리스 Chrome으로 대체했다. 첫 실행은 작업 블록을 펼치는 선택자가 바깥 `div`를 골라 시간 초과 → 버튼만 고르도록 고침. 상태 코드 셀 선택자가 표 머리글("Code")을 읽음 → 응답 행으로 고쳐 재실행
- 9단계 커밋 CI(run 40289889584, `97c0d75`): 6개 조합 성공, 스모크 6/6, `##[warning]`·`##[error]` 0건
- #13 제출 ZIP 실측(스크래치 `zip-exclusion-check.sh` — 저장소 사본에서 SKILL 7단계 명령 그대로):
  - 사본에 실제로 있던 `build/`(파일 115개, 테스트 DB 2개)·`.gradle/`(14개)·더미 `target/`·루트와 하위 폴더의 `*.db`·`*.db-journal`·`*.db-wal` → ZIP에서 모두 빠짐. `.git/`·`.idea/`·`memory-bank/`·`*.jsonl`·`settings.local.json`도 빠짐
  - 포함: `src/main`·`src/test`·`build.gradle`·`settings.gradle`·Wrapper·`prompts/used_prompts.md`·명세 사본(163항목, 216KB). **`README.md`는 아직 없음(12단계)**. `gradlew` `-rwxr-xr-x`(ZIP 안·압축 해제 뒤), 줄바꿈 `gradlew` LF·`gradlew.bat` CRLF, 로컬 절대 경로·회사 식별자·키 패턴 0건
- #13 선택 사항: 설계가 없음을 확인(D-14는 처리 시점만) → D-21로 OPT-01~03 설계(rules api·security·persistence·architecture·testing, 체크리스트 OPT·R-13). 근거 실측:
  - sqlite3: 마지막 사용자 행 삭제 → 새 가입자가 같은 id를 받음(탈퇴를 행 삭제로 하면 탈퇴자의 남은 토큰이 새 계정에 닿음), 탈퇴 표시만 하면 새 id. 기본값 없는 NOT NULL 열 추가는 실패, `default false`면 성공
  - 소스: Hibernate 6.6.53 SQLite 방언은 PK를 표 끝 `primary key (id)`로 만들어 AUTOINCREMENT 불가, 스키마 갱신은 기존 표에 `add column` 시도. Spring Data JPA 3.5.13 `Containing`은 `%`·`_` 이스케이프. Spring 6.2.19 레코드 `@ModelAttribute` 검증 실패는 `MethodArgumentNotValidException`
- #13 평가 기준·실격 조건 점검: 명세 전 항목 재대조(README·최종 ZIP 외 충족), SKILL 4단계 정적 점검 8항목 통과(도메인 순수성·고정 키·사용 금지 기능·테이블 생성 경로·커밋 작성자 27개 모두 noreply·버전·원격 추적 파일·회사 식별자). 남은 실격 위험은 README 미작성 하나(12단계)
- 다음: 사용자 확인(10단계 진행, OPT 구현 승인)

### 2026-10-26 · S13 9단계 전 명세 전수 재대조(실격 위험 점검), 서명 키 환경 변수 이름 변경, README 명령 CI 실측
- 사용자 결정: TTL은 yml 유지(D-06), Windows·Linux 실행 검증은 GitHub Actions로 실측(D-18)
- 명세 재대조: Notion 원문 재수집 — 변경 없음. 기술 조건·테이블 생성·SQLite 설정·API 8개·인증·필수 테스트 ✅, README·제출물·선택 사항은 해당 단계에서 처리
- 실격 조건 1 위험 점검:
  - 재현: 다른 프로젝트용 값 `JWT_SECRET='my-jwt-secret-for-another-project'`가 셸에 있으면 `bootRun`이 "JWT_SECRET은 Base64 문자열이어야 합니다."로 기동 실패(스크래치 `jwt-collision.log`) → 환경 변수를 `TODO_JWT_SECRET`으로 변경(`application.yaml`, `JwtConfig` 메시지, rules·CLAUDE.md·SKILL·D-06, 커밋 `0e419f3`)
  - 확인(`env-rename-check.txt`): JDK 17 `./gradlew test` 17건 통과. ① `JWT_SECRET`만 있음 → 기동·가입 201(미설정 WARN) ② `TODO_JWT_SECRET`에 잘못된 값 → 기동 실패("TODO_JWT_SECRET은 Base64 문자열이어야 합니다.") ③ 올바른 32바이트 Base64 → 기동·가입 201. 모두 로그에 키 값 0건. 첫 실행에서 ①이 BUILD FAILED로 기록됐으나 단독·전체 재실행에서 재현되지 않았다(첫 로그가 덮여 원인 미확인 → 시나리오별 로그로 저장하도록 스크립트 수정)
  - 인코딩: `JAVA_TOOL_OPTIONS=-Dfile.encoding=windows-1252`로 Windows 기본 인코딩을 흉내 내 JDK 17·21 `clean build`·한국어 응답 정상. `--debug` 로그에서 Gradle 9.8.2가 javac에 `-encoding UTF-8`을 넘김 확인 → 빌드 변경 불필요(Gradle을 거치지 않은 javac 17 + cp1252는 한국어 리터럴에서 실패)
  - Windows·Linux: CI 추가(`.github/workflows/ci.yml`, `.github/ci/ReadmeSmoke.java`, 커밋 `5e37ef0`). run 40277959532: 6개 조합 성공 — 각 작업 `> Task :test` 실행·BUILD SUCCESSFUL, `todo.db` 없는 상태의 `bootRun` 뒤 스모크 10개 호출(가입 201·로그인 200·내 정보 200·토큰 없음 401·생성 201·목록 200·수정 200·삭제 204·삭제 뒤 404(한국어 detail 보존)·재발급 200), Linux·macOS `.schema` 테이블 3개·인덱스 2개, Windows `todo.db` 생성
- 실수·정정:
  - Write 도구가 `ReadmeSmoke.java`의 `\uXXXX` 이스케이프를 실제 한글로 바꿔 저장 → cp1252 흉내 실행의 비교 실패로 발견, 이스케이프로 되돌리고 `file`로 ASCII 확인 뒤 JDK 17·21 cp1252 흉내 실행 통과
  - CI Windows 작업만 작업 뒤 캐시 저장이 경고로 실패(`fileHashes.lock … Device or resource busy` — 실행 중인 서버의 Gradle 데몬이 잠금 파일을 쥠) → 로그 보관 전에 서버를 멈추는 단계 추가(재실행으로 확인)
- 12단계 반영 예정: 8080 포트가 쓰이고 있을 때의 실행 방법(실측 후), `used_prompts.md` 맨 앞 요약 검토
- 기타: 압축 뒤 대조 — `prompt-history.jsonl`의 "claude-hub …" 프롬프트는 다른 세션(`be699439…`)의 것으로 과제와 무관해 기록 대상이 아니다. 맥이 잠들어 응답이 끊김 → 사용자 지시로 `caffeinate -ims -w <Claude Code PID>` 실행(자동 재개 훅은 없음 — `StopFailure`는 출력 무시)
- 다음: CI 재실행 확인 → 9단계 Swagger → #13 점검·보고

### 2026-10-25~26 · S9~S12 요약 (세부는 이 파일의 git 이력)
- S9 6단계 User·Auth(TEST-01): 가입·로그인(`access_token`만)·내 정보, bcrypt 72바이트 검증·로그인 실패 같은 401, 어댑터 package-private. JDK 17·21 9건 통과, bootRun으로 HS256·`exp-iat=3600`·`{bcrypt}` 저장값 확인
- S10 명세 재대조(블록 76개 동일)·7단계 TODO(TEST-02·04·X2): 소유권 404 은닉, SQLite ID 재사용 재현(R-7), 동시 요청 120건(R-3 해소), 사용자 지시로 Result → Response 변환·참고 프로젝트 네이밍(D-20). 14건 통과
- S11 테이블은 기동 때 `ddl-auto`로 생성(실행 방법과 무관) 실측, JDK 17~27 실행 범위 점검 — JDK 27에서 Lombok 1.18.46 컴파일 실패 발견(R-9)
- S12 협의 반영(Lombok 1.18.48·`memory-bank/` 제외), 8단계 재발급 확장(TEST-07·08·09): 회전·재사용 탐지(`noRollbackFor`)·만료, 변이 4종, 서버 재시작 뒤 재발급 실측, TTL 위치 검토(yml 유지). 17건 통과

### 2026-10-25 · S1~S8 요약 (세부는 이 파일의 git 이력)
- S1 설계 검증과 작업 체계 수립: CLAUDE.md 초안 458줄 → 72줄, rules·skill·docs 작성. Boot 4.1.2·Gradle 9.8.2 생성물, SQLite FK DDL 미생성, BCrypt 72바이트 예외, NimbusJwtEncoder 기본 RS256, ERROR 디스패치 401 함정 확인
- S2 명세 원문 수집(블록 76개)·대조, 사용자 결정 3건(빌드 기준선·Java 17 타깃·springdoc 유지), `memory-bank/` 백업 후 삭제, `git init`
- S3 저장소 정리·원격 연결, 테이블 생성 경로(D-10)·재발급 확장(D-15) 설계
- S4 빌드 기준선 교정(Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17), 커밋 작성자를 닉네임 + noreply로 재작성, 프롬프트 기록 훅·백필(D-16)
- S5 저장소 재생성 확인, 푸시는 저장소 로컬 헬퍼(`gh auth token --user manners-parkpro`), JDK 17 기본 + 21 교차 확인(사용자 결정)
- S6 4단계 설정·테스트 기반(SQLite·풀 1·`ddl-auto: update`, 테스트 DB `build/todo-test.db`). springdoc 문서 엔드포인트 기본 활성 WARN 2건은 9단계에서 처리 검토
- S7 압축 뒤 프롬프트 기록·CLAUDE.md 로드 검증, 훅 추가(`log-instructions.py`·`guard-git.py`), `step-reviewer`, 설계 문서 리팩터링(독립 리뷰·best practices 대조)
- S8 사용자 참조 이름 `userId` 결정, 5단계 보안 골격(TEST-03) — ProblemDetail 엔트리 포인트, 공개 인증 경로 Bearer 헤더 무시, 기본 `logout` 필터 비활성(리뷰 지적 재현 후)
