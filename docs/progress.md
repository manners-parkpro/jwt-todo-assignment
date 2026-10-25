# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-25
- **단계: 2단계(저장소 정리·원격 연결) 완료 → 초기 커밋·푸시(사용자 승인) → 3단계 대기.** 다음 단계는 사용자 확인 후 진행한다(CLAUDE.md 단계 게이트).
- 설계 갱신: 테이블 생성 경로 단일화(D-10), 토큰 재발급 확장(D-15, `POST /users/refresh`), 공개 저장소 운영(D-13).
- 빌드·테스트: 미실행. 기존 `build.gradle`은 Boot 4.1.2·toolchain 21이라 3단계에서 교정한다.

## 다음 작업 (한 단계씩 — 단계마다 검증 결과 보고 후 사용자 확인)
1. [x] 사용자 확인 사항 처리 — 명세 확보, D-01·D-02 결정, `memory-bank/` 삭제(백업), `git init`
2. [x] 저장소 정리·원격 연결 — `.gitignore` 보강, Initializr 잔여물 삭제, origin 연결 (검증: S3 로그) — DB-05·SUB-04·SUB-05(부분)
3. [ ] 빌드 기준선 교정(D-01): `build.gradle` Boot 3.5.16 · `options.release = 17` · 의존성 정리 · `tomcat.version` 10.1.61 · springdoc 2.8.17 · Lombok은 main만 → JDK 17·21에서 `compileJava`, `dependencyInsight` — TECH-01·02·04·05·08, DB-01
4. [ ] 설정과 테스트 베이스: `application.yaml`(`auth.*` 포함), `application-test.yaml`, `IntegrationTestSupport` → JDK 17·21에서 `./gradlew clean test --warning-mode all`(contextLoads) — TECH-03(Gradle 9.8.2 조합 실측), TEST-05, DB-04
5. [ ] 보안 골격: SecurityConfig·JWT 빈(임의 키 대체 포함)·공개 인증 경로용 `BearerTokenResolver`·EntryPoint·GlobalExceptionHandler → TEST-03 — SEC-02·04·07·08·12, ERR-01
6. [ ] User·Auth(가입·내 정보·로그인 — 액세스·리프레시 토큰 발급, `refresh_tokens`) → TEST-01 — API-01~03, SEC-01·03·06
7. [ ] Todo 기능 → TEST-02·04, 클린 상태에서 README 명령만으로 `.schema` 확인 — API-04~09, ERR-02~04, DB-02·03, TECH-06
8. [ ] 토큰 재발급(회전·재사용 탐지) → TEST-07·08 — API-11, SEC-11
9. [ ] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10
10. [ ] 선택 테스트 TEST-X1~X4, 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06
11. [ ] README 작성과 명령 실검증(macOS/Linux·Windows 병기, SQL 명령 없음) — DOC-01~05, TECH-07
12. [ ] 선택 기능 OPT-01~03 결정 — D-14
13. [ ] `/submission-check <이름>` — SUB-01~05, SEC-09·10, DOC-06

## 환경 메모
- JDK: Corretto 21.0.10(기본), OpenJDK 17.0.18도 설치됨 → 두 버전으로 검증. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개 저장소). `docs/assignment-spec.md`는 `.gitignore`로 제외

## 세션 로그 (최신이 위)

### 2026-10-25 · S3 2단계 저장소 정리·원격 연결, 재발급 확장 설계
- 완료:
  - `.gitignore`에 `### Project ###` 섹션 추가(`*.db`, `*.db-*`, `.env`, `.env.*`, `.DS_Store`, `.claude/settings.local.json`, `docs/assignment-spec.md`)
  - `HELP.md`, 빈 `src/main/resources/static/`·`templates/` 삭제
  - `git remote add origin https://github.com/manners-parkpro/jwt-todo-assignment.git`
  - 설계: D-10(테이블 생성 경로 단일화)·D-15(재발급 확장) 추가·갱신, rules(security·persistence·api·architecture·testing·docs)·skill·체크리스트 갱신
- 실행 명령과 결과:
  - `git check-ignore -v --no-index` → `todo.db`·`build/todo-test.db`·`todo.db-journal`·`todo.db-wal`·`.env`·`.env.local`·`.DS_Store`·`src/.DS_Store`·`.claude/settings.local.json`·`build/`·`.gradle/` 모두 무시됨
  - 추적 대상 확인 → `gradle/wrapper/gradle-wrapper.jar`, `gradlew(.bat)`, `.claude/**`, `CLAUDE.md`, `prompts/used_prompts.md`, `src/**` 무시되지 않음
  - `git ls-remote origin` → 출력 없음, 종료 코드 0(빈 저장소). GitHub API(인증 없음) → HTTP 200(공개 저장소)
  - 첫 `timeout 30 git ls-remote` 시도는 macOS에 `timeout`이 없어 실패(코드 127) → 프롬프트를 끈 `git ls-remote`로 재실행
- 커밋·푸시: 사용자 승인에 따라 이 기록을 포함해 초기 커밋을 만들고 `origin/main`에 푸시한다. 푸시 검증 결과는 다음 로그에 남긴다.
- 다음: 사용자 확인 후 3단계(빌드 기준선 교정)

### 2026-10-25 · S2 명세 대조와 설계 최적화
- 완료: Notion 명세 원문 수집(공개 API, 블록 76개) → `docs/assignment-spec.md`. 명세·초안·설계 대조. 보안·YAGNI·재현성 재검토와 사용자 결정 3건(빌드 기준선, Java 17 타깃, springdoc 유지). `memory-bank/` 백업 후 삭제, `git init`
- 결과: 빌드·테스트 미실행(설계 단계). 주요 발견 — 명세에 Boot 버전 조건 없음(JUnit 5 필수 → 3.5.16 유지), Gradle 8.14는 JDK 25 이상에서 실행 불가, 3.5 계열 CVE의 전제 기능 미사용, Tomcat OSS 패치 존재, springdoc의 전역 네이밍 전략 미반영, SQLite 테이블 단위 유니크 제약 생략

### 2026-10-25 · S1 설계 검증과 작업 체계 수립
- 완료: 기존 프로젝트·환경 분석, best practice 검증(공식 문서·Maven Central·소스 코드), CLAUDE.md 재작성(초안 458줄 → 72줄), rules·skill·docs 작성, `prompts/used_prompts.md` #1
- 결과: 빌드·테스트 미실행(설계 단계). 주요 발견 — Boot 4.1.2·Gradle 9.8.2 생성물, SQLite FK DDL 미생성, BCrypt 72바이트 예외, NimbusJwtEncoder 기본 RS256, ERROR 디스패치 401 함정
