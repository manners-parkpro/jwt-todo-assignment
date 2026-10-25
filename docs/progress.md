# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-25
- **단계: 5단계(보안 골격) 완료 → 6단계 대기.** 설계 승인은 2026-10-25(used_prompts #2). 다음 단계는 사용자 확인 후 진행한다(CLAUDE.md 단계 게이트).
- 빌드·테스트: Gradle 9.8.2 + Boot 3.5.16, JDK 17·21 모두 `clean build` 통과(8건 — contextLoads·TEST-03, S8). `bootRun`에서 보안 동작 확인(S8).
- 원격: 저장소 로컬 자격 증명 헬퍼로 푸시한다. 원격 커밋 작성자·커미터는 모두 noreply다.
- 지침·기록(S7): 압축 뒤 CLAUDE.md 전체 주입 확인. 로컬 훅 3개 — 프롬프트 원문 백업(하네스 메시지 제외), 지침 로드 기록, 커밋·푸시 검사(작성자·테스트·강제 푸시).

## 다음 작업 (한 단계씩 — 계획·구현·검증·독립 리뷰·보고 후 사용자 확인)
1. [x] 사용자 확인 사항 처리 — 명세 확보, D-01·D-02 결정, `memory-bank/` 삭제(백업), `git init`
2. [x] 저장소 정리·원격 연결 — `.gitignore` 보강, Initializr 잔여물 삭제, origin 연결
3. [x] 빌드 기준선 교정 — `build.gradle` Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17·Lombok main만 (검증: S4)
4. [x] 설정과 테스트 베이스 — `application.yaml`(SQLite·JPA), `application-test.yaml`, `IntegrationTestSupport` (검증: S6). `auth.*`는 사용하는 코드와 함께 5단계로 조정
5. [x] 보안 골격: `auth.jwt-secret`·SecurityConfig·서명 키와 JWT 디코더(임의 키 대체)·공개 인증 경로용 `BearerTokenResolver`·EntryPoint·GlobalExceptionHandler → TEST-03 — SEC-02·04·07·08·12, ERR-01 (검증: S8)
6. [ ] User·Auth 필수 범위: `JwtEncoder`(HS256 헤더 명시)·`TokenIssuer`·`PasswordEncoder`·`@CurrentUserId`, 가입·내 정보·로그인(**`access_token`만 발급**) → TEST-01 — API-01~03, SEC-01·03·06
7. [ ] Todo 기능 → TEST-02·04, 클린 상태 `bootRun`으로 `users`·`todos` `.schema` 중간 확인 — API-04~09, ERR-02·03, DB-02·03
8. [ ] 재발급 확장: 로그인 응답에 `refresh_token` 추가, `refresh_tokens`, `POST /users/refresh`(회전·재사용 탐지, 공개 엔드포인트 매처에 추가) → TEST-07·08 + **TEST-01~04 회귀**, `refresh_tokens` `.schema` 확인 — API-11, SEC-11, DB-06
9. [ ] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10
10. [ ] 선택 테스트 TEST-X1~X4, 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06, ERR-04
11. [ ] 선택 기능 OPT-01~03 결정(구현하면 테스트 포함) — D-14
12. [ ] README 작성과 명령 실검증(최종 API 기준, macOS/Linux 실측 + Windows 표준 Wrapper 명령, SQL 명령 없음), 클린 상태에서 README 명령으로 테이블 생성 최종 확인 — DOC-01~05, TECH-06·07, D-18
13. [ ] `/submission-check <이름>` — SUB-01~06, SEC-09·10, DOC-06~08

## 환경 메모
- JDK: 이 PC 기본은 Corretto 21.0.10. 기본 검증은 OpenJDK 17.0.18이며, 셸 환경 변수가 Bash 호출마다 초기화되므로 명령마다 `JAVA_HOME=$(/usr/libexec/java_home -v 17)`을 붙인다. `java -version`은 PATH 앞쪽의 Corretto 21을 가리키므로 사용 JDK는 `./gradlew --version`의 Launcher JVM으로 확인한다. 단계 완료 시 21로 교차 확인. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip·jq 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개). 저장소 로컬 작성자 = `Colin <43167000+manners-parkpro@users.noreply.github.com>`. `docs/assignment-spec.md`, `.claude/settings.local.json`, `.claude/prompt-history.jsonl`은 `.gitignore`로 제외. 푸시 인증은 저장소 로컬 `credential.https://github.com.helper`가 `gh auth token --user manners-parkpro`를 사용(gh 활성 계정·전역 설정은 그대로)
- 로컬 훅(설정은 `.claude/settings.local.json`): `log-prompt.py`(UserPromptSubmit → `.claude/prompt-history.jsonl`, `<task-notification>`·`<agent-message` 제외 — 그 전에 기록된 4건은 대조할 때 건너뜀), `log-instructions.py`(InstructionsLoaded → `.claude/instructions-log.jsonl`), `guard-git.py`(PreToolUse Bash — 커밋 때 JDK 17 테스트로 약 10초). 세션 중에 만든 에이전트·지침 파일은 그 세션에서 로드되지 않을 수 있다 — `step-reviewer`를 고를 수 없으면 같은 파일 내용으로 general-purpose 에이전트를 쓴다

## 세션 로그 (최신이 위)

### 2026-10-25 · S8 사용자 참조 이름 결정(userId), 5단계 보안 골격
- 결정: TODO의 사용자 참조를 `userId`·`user_id`로 통일(S7의 `owner_id` 철회 — Boot 3.5.16 기본 명명 전략 확인, D-10). 커밋 `eca0ee0`
- 구현: `auth.jwt-secret`, `JwtConfig`(미설정 시 임의 키 + WARN, Base64·32바이트 검증, HS256 디코더·시계 오차 0), `SecurityConfig`(CSRF·폼 로그인·Basic·로그아웃 비활성, STATELESS, ERROR 디스패치·가입·로그인·Swagger permitAll, 공개 경로 Bearer 헤더 무시), `ProblemDetailAuthenticationEntryPoint`, `GlobalExceptionHandler`, `AuthenticationRequiredTest`(TEST-03)
- 범위 조정: `JwtEncoder`·`PasswordEncoder`·`@CurrentUserId`는 쓰는 코드와 함께 6단계, `/users/refresh`는 8단계로(문서 반영)
- 실행 명령과 결과:
  - Spring Security 6.5.11 소스: 엔트리 포인트는 `setStatus`만(본문 없음), STATELESS → `NullRequestCache`, `AntPathRequestMatcher` 제거 예정 → `PathPatternRequestMatcher`, 기본 시계 오차 60초
  - 첫 `./gradlew test` 컴파일 실패(위임 필드를 인터페이스 타입으로 선언해 `ServletException` 전파) → 구체 타입으로 수정
  - JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=8 failures=0, `todo.db` 없음
  - bootRun + curl(JDK 17): 토큰 없음 401 `WWW-Authenticate: Bearer`·ProblemDetail, 형식 오류 토큰 401 `invalid_token`, Basic 헤더 401, 세션 쿠키 없음, `POST /users/login` + 엉터리 Bearer → 404 ProblemDetail(헤더 무시), `/v3/api-docs` 200, 키 미설정 WARN, Base64 아님·16바이트 키 → 기동 실패(로그에 키 값 없음), 32바이트 키 → 경고 없음
  - 독립 리뷰(`step-reviewer` 첫 사용): 범위 내 결함 없음, 낮음 2건 — 기본 로그아웃 필터가 `/logout`을 인증 없이 302 처리(재현 후 `logout` 비활성 → 401 확인), `detail` 언어 규칙과 프레임워크 기본값 불일치(규칙을 직접 작성하는 detail로 한정, D-07). 근거 로그·문구 정확성 지적도 반영
- 실수: 스모크 스크립트가 라벨을 curl 인자로 넘겨 출력 순서가 섞임(표시된 상태·헤더·본문은 실제 요청 결과)
- 다음: 사용자 확인 후 6단계

### 2026-10-25 · S7 압축 뒤 점검 — 프롬프트 기록·CLAUDE.md 로드 검증, 설계 문서 리팩터링(독립 리뷰·best practices 대조)
- 확인(프롬프트): 백업 15줄과 `used_prompts` 대조 — 사용자 프롬프트 누락 0건(#1~#5에 원문, 계정명 가림·초안은 요지), 이번 프롬프트는 #6. 압축 뒤 첫 프롬프트도 훅이 기록. 비사용자 줄 5건: `/effort`, `<task-notification>` 2, `<agent-message` 2
- 확인(CLAUDE.md 로드): 압축 뒤 트랜스크립트 주입본(4,820자) = 디스크 파일(유지보수 HTML 주석 3줄·끝 줄바꿈만 다름 — 설계대로 제거). 경로 규칙은 압축 뒤 `docs.md` 재로드, rules를 직접 Read한 뒤에는 재주입 안 됨(중복 방지), 새 컨텍스트 서브에이전트에서 `build.md`·`architecture.md`·`security.md` 로드 → 강제 재주입 훅은 불필요(D-17)
- 추가: `log-instructions.py`, `guard-git.py`, `.claude/agents/step-reviewer.md`, `log-prompt.py` 하네스 메시지 제외, `.gitignore`에 `instructions-log.jsonl`
- 문서: 독립 리뷰 서브에이전트 지적과 best practices 대조 반영 — JDK 명령마다 지정, 단계·ID 매핑(DB-06 분리, ERR-04 → 10단계, TECH-06 최종 확인 12단계), README·OPT 순서 교체, SKILL 기록→ZIP 순서·SEC-09 점검, Windows 명령 범위(D-18), 요청 record `@JsonProperty`, 모든 401 `WWW-Authenticate`, `@Value`·기능 예외 위치, todos `owner_id`, 오래된 진술 정정, CLAUDE.md 정리(도출 가능한 줄·중복 행 삭제, 커밋 규칙·리뷰 단계 추가)
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → BUILD SUCCESSFUL, tests=1 failures=0 (Launcher JVM 17.0.18)
  - JDK 17·21 `./gradlew clean build --warning-mode all` → 둘 다 BUILD SUCCESSFUL, tests=1, bootJar·plain jar, `todo.db` 없음. 경고: Boot 플러그인 deprecation 1건, 테스트 JVM CDS 안내 1건(영향 없음)
  - 훅 파이프 테스트: log-prompt(하네스 메시지 2종 제외, `<`로 시작하는 일반 입력은 기록, 잘못된 JSON은 종료 1), log-instructions(글자 수·해시가 `wc -m`·`shasum`과 일치), guard-git 15건(허용 6·거부 9). 설정 뒤 실제 동작: InstructionsLoaded 3건 기록, guard-git이 무해한 `echo` 거부
  - 원격: `origin/main` = 로컬 `48735c6`, 커밋 6개 작성자·커미터 모두 noreply, 명세 사본 없음, 추적 파일에 회사 식별자 없음. sqlite-jdbc jar에 Windows 네이티브 포함
- 실수: `py_compile` 점검이 `.claude/hooks/__pycache__`를 만들어 삭제. rules를 직접 Read한 뒤 재로드를 시험해 처음엔 로드 실패로 오해 → 새 컨텍스트 서브에이전트로 재확인
- 다음: 사용자 확인 후 5단계

### 2026-10-25 · S6 4단계 설정과 테스트 기반
- 완료: `application.yaml`(SQLite `todo.db`, Hikari 풀 1, SQLiteDialect, `ddl-auto: update`, `open-in-view: false`), `src/test/resources/application-test.yaml`(`build/todo-test.db`, `create-drop`), `IntegrationTestSupport`(`@SpringBootTest`·`@AutoConfigureMockMvc`·`@ActiveProfiles("test")`), `AssignmentApplicationTests`가 베이스 상속. `auth.*` 설정은 쓰는 코드와 함께 5단계로 옮김(쓰이지 않는 설정 방지)
- 실행 명령과 결과:
  - JDK 17 `./gradlew clean test --warning-mode all` → BUILD SUCCESSFUL, tests=1 failures=0 errors=0. Hibernate 6.6.53, SQLite 3.49.1 인식, HikariPool-1 시작. 실행 전후 `todo.db` 없음(테스트는 `build/todo-test.db`)
  - JDK 17 `./gradlew bootRun` → Started AssignmentApplication(약 3.8초), Tomcat 10.1.61, `GET /` 401(보안 설정 전 기본 동작), `todo.db` 생성·git 무시 확인 후 삭제
  - SIGTERM 종료 → graceful shutdown·JPA·Hikari 종료 정상. Gradle은 종료 코드 143(SIGTERM)을 빌드 실패로 보고 — 백그라운드 작업 종료 코드 1의 원인이며 정상
  - JDK 21 `./gradlew clean test` → BUILD SUCCESSFUL, tests=1, `todo.db` 없음
  - WARN: springdoc 문서 엔드포인트 기본 활성 안내 2건(의도된 유지 — 9단계 springdoc 설정 때 처리 검토)
- 실수: `bootRun` 로그를 `build/`에 저장해 이어진 `clean`이 지웠다 → 스크래치패드에 로그를 두고 재현해 종료 과정을 확인
- 다음: 사용자 확인 후 5단계

### 2026-10-25 · S5 저장소 재생성 확인, 푸시 계정 방식 검증, JDK 검증 정책
- 확인: 재생성된 저장소는 처음에 비어 있었다(앞선 푸시 미반영). 이전 커밋 `52809a9` 조회 HTTP 409. 훅이 일반 프롬프트와 작업 중 메시지를 `source: "hook"`으로 기록함
- 문제: `git push`가 403 — 전역 git 설정의 github.com 헬퍼가 gh CLI이고, gh 헬퍼는 활성 계정(다른 계정) 토큰만 반환(gh 2.90.0 소스 확인)
- 검증: `git push --dry-run`으로 (A) 다른 계정에 권한 부여, (C) 저장소 로컬 헬퍼가 `gh auth token --user manners-parkpro` 사용 — 둘 다 성공. 원격 URL에 사용자명을 넣는 방식은 gh가 따르지 않아 되돌림
- 결정(사용자): C안, JDK는 17 기본 + 단계 완료 시 21 교차 확인
- 결과: `git push -u origin main` 성공, 원격 해시 = 로컬 `98a2de2`, 원격 커밋 3개 모두 noreply·manners-parkpro 연결(GitHub API). gh 활성 계정·전역 설정 변경 없음. 공개 이벤트 API의 푸시 계정은 반영 지연으로 아직 미확인
- 다음: 4단계

### 2026-10-25 · S4 3단계 빌드 기준선 교정, 커밋 작성자 정정, 프롬프트 기록 보존
- 완료:
  - `build.gradle` 교정: Boot 4.1.2 → 3.5.16, toolchain 21 제거 → `options.release = 17`, Boot 4 전용 스타터 → web·data-jpa·validation·oauth2-resource-server·test, `hibernate-community-dialects` 추가, springdoc 3.1.0 → 2.8.17, Lombok 테스트 의존성 제거, `ext['tomcat.version'] = '10.1.61'`
  - 필수 테스트 보호 규칙화(CLAUDE.md 완료 기준, testing.md, api.md 구현 순서, D-15): 로그인은 6단계에서 `access_token`만, `refresh_token`은 8단계에서 추가 후 TEST-01~04 회귀
  - 커밋 작성자: 저장소 로컬 git 설정을 닉네임 + GitHub noreply로 바꾸고 초기 커밋을 `git commit --amend --reset-author`로 다시 만듦(`52809a9` → `44dd876`, 트리 동일)
  - 프롬프트 기록: `UserPromptSubmit` 훅 추가, 이번 세션 프롬프트 7건 백필(D-16), CLAUDE.md 세션 루틴·압축 보존 항목 갱신
- 실행 명령과 결과:
  - `./gradlew --version` → Gradle 9.8.2(JDK 21 / JDK 17 각각 실행 확인)
  - `./gradlew clean compileJava compileTestJava assemble --warning-mode all` → JDK 21·17 모두 BUILD SUCCESSFUL. 경고 1건: Boot 플러그인 `JavaPluginAction`의 `Configuration.setVisible` 사용(Gradle 11 제거 예정)
  - `javap -v … AssignmentApplication` → major version 61(Java 17)
  - `./gradlew dependencies`·`dependencyInsight` → spring-boot 3.5.16, spring-core 6.2.19, spring-security-oauth2-jose 6.5.11, hibernate-core·community-dialects 6.6.53.Final, sqlite-jdbc 3.49.1.0, tomcat-embed-core 10.1.61(rule), springdoc 2.8.17, junit-jupiter 5.12.2, Lombok은 main compileClasspath에만
  - 훅: 파이프 테스트(정상 종료 0·stdout 없음·권한 600·원문 일치·덧붙이기, 실패 종료 1), `jq -e` 스키마 검증, 셸 형식 명령 실행. 백필 7건, 복원 실패 0(초안 원문 18.7KB를 붙여넣기 캐시에서 복원)
  - 프롬프트 기록 실측: 트랜스크립트·`paste-cache`는 `cleanupPeriodDays`(기본 30일) 후 삭제 대상, `history.jsonl`은 유지되지만 큰 붙여넣기는 해시 참조만 저장
- 실패·보류:
  - `git push --force-with-lease`가 자동 모드 분류기에서 거부됨(Git Destructive) → 우회하지 않음. 사용자 실행 명령: `! git push --force-with-lease=main:52809a9ef23260890de10400780b3f9a1a80e672 origin main`
  - GitHub 문서상 force push 뒤에도 이전 커밋은 SHA로 캐시된 화면에서 접근될 수 있다. 완전 제거가 필요하면 저장소를 삭제하고 다시 만든 뒤 푸시한다(사용자 결정)
  - `./gradlew test`는 실행하지 않음(데이터소스 설정 전 — 4단계)
- 다음: 사용자 force push 실행, 다음 프롬프트로 훅 실행 확인, 사용자 확인 후 4단계

### 2026-10-25 · S3 2단계 저장소 정리·원격 연결, 재발급 확장 설계
- 완료: `.gitignore` 보강, `HELP.md`·빈 `static/`·`templates/` 삭제, origin 연결, D-10·D-15 설계 반영, 초기 커밋 `52809a9` 푸시
- 결과: `git check-ignore`로 무시·추적 대상 확인, 원격이 비어 있는 공개 저장소임을 확인, 푸시 후 원격 해시 일치·명세 사본 미포함 확인

### 2026-10-25 · S2 명세 대조와 설계 최적화
- 완료: Notion 명세 원문 수집(공개 API, 블록 76개) → `docs/assignment-spec.md`. 명세·초안·설계 대조, 보안·YAGNI·재현성 재검토, 사용자 결정 3건(빌드 기준선, Java 17 타깃, springdoc 유지), `memory-bank/` 백업 후 삭제, `git init`
- 결과: 명세에 Boot 버전 조건 없음(JUnit 5 필수 → 3.5.16 유지), Gradle 8.14는 JDK 25 이상에서 실행 불가, 3.5 계열 CVE의 전제 기능 미사용, Tomcat OSS 패치 존재, springdoc의 전역 네이밍 전략 미반영, SQLite 테이블 단위 유니크 제약 생략

### 2026-10-25 · S1 설계 검증과 작업 체계 수립
- 완료: 기존 프로젝트·환경 분석, best practice 검증, CLAUDE.md 재작성(초안 458줄 → 72줄), rules·skill·docs 작성, `prompts/used_prompts.md` #1
- 결과: Boot 4.1.2·Gradle 9.8.2 생성물, SQLite FK DDL 미생성, BCrypt 72바이트 예외, NimbusJwtEncoder 기본 RS256, ERROR 디스패치 401 함정
