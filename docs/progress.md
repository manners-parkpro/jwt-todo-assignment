# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-25
- **단계: 4단계(설정·테스트 기반) 완료 → 5단계 대기.** 다음 단계는 사용자 확인 후 진행한다(CLAUDE.md 단계 게이트).
- 빌드·테스트: Gradle 9.8.2 + Boot 3.5.16, JDK 17·21 모두 `clean test` 통과(contextLoads). `bootRun` 기동·정상 종료 확인.
- 원격: 사용자가 저장소를 재생성했고, 저장소 로컬 자격 증명 헬퍼(`gh auth token --user manners-parkpro`)로 푸시했다. 원격 커밋 작성자는 모두 noreply다.
- 프롬프트 원문 백업 훅: 실제 프롬프트와 작업 중 메시지가 기록되는 것을 확인했다(S5).

## 다음 작업 (한 단계씩 — 단계마다 검증 결과 보고 후 사용자 확인)
1. [x] 사용자 확인 사항 처리 — 명세 확보, D-01·D-02 결정, `memory-bank/` 삭제(백업), `git init`
2. [x] 저장소 정리·원격 연결 — `.gitignore` 보강, Initializr 잔여물 삭제, origin 연결
3. [x] 빌드 기준선 교정 — `build.gradle` Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17·Lombok main만 (검증: S4)
4. [x] 설정과 테스트 베이스 — `application.yaml`(SQLite·JPA), `application-test.yaml`, `IntegrationTestSupport` (검증: S6). `auth.*`는 사용하는 코드와 함께 5단계로 조정
5. [ ] 보안 골격: `auth.*` 설정·SecurityConfig·JWT 빈(임의 키 대체)·공개 인증 경로용 `BearerTokenResolver`·EntryPoint·GlobalExceptionHandler → TEST-03 — SEC-02·04·07·08·12, ERR-01
6. [ ] User·Auth 필수 범위: 가입·내 정보·로그인(**`access_token`만 발급**) → TEST-01 — API-01~03, SEC-01·03·06
7. [ ] Todo 기능 → TEST-02·04, 클린 상태에서 README 명령만으로 `.schema` 확인 — API-04~09, ERR-02~04, DB-02·03, TECH-06
8. [ ] 재발급 확장: 로그인 응답에 `refresh_token` 추가, `refresh_tokens`, `POST /users/refresh`(회전·재사용 탐지) → TEST-07·08 + **TEST-01~04 회귀** — API-11, SEC-11
9. [ ] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10
10. [ ] 선택 테스트 TEST-X1~X4, 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06
11. [ ] README 작성과 명령 실검증(macOS/Linux·Windows 병기, SQL 명령 없음) — DOC-01~05, TECH-07
12. [ ] 선택 기능 OPT-01~03 결정 — D-14
13. [ ] `/submission-check <이름>` — SUB-01~06, SEC-09·10, DOC-06·07

## 환경 메모
- JDK: 이 PC 기본은 Corretto 21.0.10. 기본 검증은 OpenJDK 17.0.18(`JAVA_HOME=$(/usr/libexec/java_home -v 17)`), 단계 완료 시 21로 교차 확인. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip·jq 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개). 저장소 로컬 작성자 = `Colin <43167000+manners-parkpro@users.noreply.github.com>`. `docs/assignment-spec.md`, `.claude/settings.local.json`, `.claude/prompt-history.jsonl`은 `.gitignore`로 제외. 푸시 인증은 저장소 로컬 `credential.https://github.com.helper`가 `gh auth token --user manners-parkpro`를 사용(gh 활성 계정·전역 설정은 그대로)
- 프롬프트 원문 백업: `.claude/hooks/log-prompt.py`(UserPromptSubmit 훅, 설정은 `.claude/settings.local.json`) → `.claude/prompt-history.jsonl`

## 세션 로그 (최신이 위)

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
