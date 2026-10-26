# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-26
- **단계: 9단계(Swagger) 진행 중.** 설계 승인은 2026-10-25(used_prompts #2). 사용자 지시(#12)로 명세 전수 재대조·실격 위험 보강(S13)을 마친 뒤 진행한다. 사용자 지시(#13): 9단계를 마치면 제출 ZIP 제외 항목 실측, 선택 사항(가산점) 설계 점검·보강(구현은 승인 후), 평가 기준·실격 조건 최종 점검을 보고한다.
- 빌드·테스트: JDK 17·21 `clean build` 17건 통과(S12 — contextLoads·TEST-01·02·03(7)·04(3)·07·08·09·X2). CI(GitHub Actions)에서 Windows·Linux·macOS × JDK 17·21 모두 테스트·`bootRun`·API 호출 성공(S13, run 40277959532).
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
9. [ ] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10
10. [ ] 선택 테스트 TEST-X1·X3·X4(X2는 7단계), 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06, ERR-04
11. [ ] 선택 기능 OPT-01~03 결정(구현하면 테스트 포함) — D-14
12. [ ] README 작성과 명령 실검증(최종 API 기준, macOS/Linux 실측 + Windows 표준 Wrapper 명령 — CI로 3개 OS × JDK 17·21 확인, SQL 명령 없음, 8080 포트 사용 중일 때의 실행 방법 실측), 클린 상태에서 README 명령으로 테이블 생성 최종 확인 — DOC-01~05, TECH-06·07, D-18
13. [ ] `/submission-check <이름>` — SUB-01~06, SEC-09·10, DOC-06~08

## 환경 메모
- JDK: 이 PC 기본은 Corretto 21.0.10. 기본 검증은 OpenJDK 17.0.18이며, 셸 환경 변수가 Bash 호출마다 초기화되므로 명령마다 `JAVA_HOME=$(/usr/libexec/java_home -v 17)`을 붙인다. `java -version`은 PATH 앞쪽의 Corretto 21을 가리키므로 사용 JDK는 `./gradlew --version`의 Launcher JVM으로 확인한다. 단계 완료 시 21로 교차 확인. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip·jq 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개). 저장소 로컬 작성자 = `Colin <43167000+manners-parkpro@users.noreply.github.com>`. `docs/assignment-spec.md`, `.claude/settings.local.json`, `.claude/prompt-history.jsonl`은 `.gitignore`로 제외. 푸시 인증은 저장소 로컬 `credential.https://github.com.helper`가 `gh auth token --user manners-parkpro`를 사용(gh 활성 계정·전역 설정은 그대로)
- 로컬 훅(설정은 `.claude/settings.local.json`): `log-prompt.py`(UserPromptSubmit → `.claude/prompt-history.jsonl`, `<task-notification>`·`<agent-message` 제외 — 그 전에 기록된 4건은 대조할 때 건너뜀), `log-instructions.py`(InstructionsLoaded → `.claude/instructions-log.jsonl`), `guard-git.py`(PreToolUse Bash — 커밋 때 JDK 17 테스트로 약 10초). 세션 중에 만든 에이전트·지침 파일은 그 세션에서 로드되지 않을 수 있다 — `step-reviewer`를 고를 수 없으면 같은 파일 내용으로 general-purpose 에이전트를 쓴다

## 세션 로그 (최신이 위)

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

### 2026-10-26 · S12 협의 결과 반영(Lombok 1.18.48, memory-bank 제외), 8단계 재발급 확장(TEST-07·08·09)
- 반영: `build.gradle`에 `ext['lombok.version'] = '1.18.48'`(build.md·D-01·D-02), `.gitignore`에 `memory-bank/`, SKILL ZIP 제외 목록에 `memory-bank/*`(D-13), persistence.md에 테이블 생성 시점·DB 위치(S11 실측) 명시
- 실행 명령과 결과:
  - `dependencyInsight` → `org.projectlombok:lombok:1.18.48 (selected by rule)`
  - JDK 17 `./gradlew test` → 14건 통과. JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=14 failures=0
  - JDK 27(Corretto 27.0.1, 이 수정의 일회 확인) `./gradlew clean build` → BUILD SUCCESSFUL, tests=14 failures=0
  - `git check-ignore -v memory-bank/activeContext.md` → `.gitignore:50`. SKILL과 같은 명령으로 만든 시험 ZIP: `build`·`.gradle`·`memory-bank`·`.idea`·`.git`·`*.db`·`*.jsonl` 없음, 필수 파일 7종 포함, `gradlew` `-rwxr-xr-x`가 ZIP 안과 `unzip` 뒤 모두 유지(R-8) — 시험 ZIP은 확인 후 삭제
- 답이 없던 항목: Windows 실측(3번)은 현행 유지(D-18), README 안내(4번)는 12단계 계획대로
- 8단계 구현 전 재검토(보고 후 반영): TEST-09(리프레시 토큰 만료) 추가, 테스트 도우미 분리로 필수 테스트 보호, 검증 오류 `field`를 JSON 이름으로, 해시는 SHA-256 유지(HMAC은 재시작 시 무효화), 알려진 한계 3가지 기록(D-15·D-07)
- 8단계 구현: 도메인 `RefreshToken`, `InvalidRefreshTokenException`, `RefreshCommand`·`TokenResult`(토큰 쌍), `RefreshTokenCommandPort`·`RefreshTokenQueryPort`, `AuthService`(로그인 시 만료 행 정리·토큰 쌍 발급, 재발급의 회전·재사용 탐지 — `noRollbackFor`), `RefreshTokenJpaEntity`·`RefreshTokenRepository`·`RefreshTokenRepositoryAdapter`, `RefreshRequest`·`TokenResponse`·`POST /users/refresh`, 공개 경로 매처, 401 매핑, `auth.refresh-token-ttl: 7d`, 테스트 `TokenRefreshFlowTest`(TEST-07)·`RefreshTokenRotationTest`(TEST-08)·`RefreshTokenExpiryTest`(TEST-09)
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → 17건 통과(첫 실행)
  - 변이 4종(되돌린 뒤 `cmp` 확인): ① 로그인 응답 `refresh_token` null → TEST-07·08만 실패, **TEST-01~04 통과**(테스트가 `refresh_token`에 의존하지 않음. 확장 없이 통과하는 것은 7단계 커밋 `d741470`에서 성립) ② `noRollbackFor` 제거 → TEST-08 실패(폐기 롤백 함정 검출) ③ 만료 확인 제거 → TEST-09 실패 ④ 재발급 경로를 공개 매처에서 제외 → TEST-07·08·09 실패
  - JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=17 failures=0. 정적 점검(web→domain·application 프레임워크·도메인 순수성·인스턴스화 형식) 위반 0건
  - bootRun + curl(`step8-smoke-report.txt`): `refresh_tokens(token_hash varchar(64) not null unique …)` + `idx_refresh_tokens_user_id`, 로그인 200 `{access_token, refresh_token}`(43자 Base64URL)·`Cache-Control: no-cache, no-store, max-age=0, must-revalidate`, 엉터리 Bearer + 재발급 200·새 토큰 다름, 쓴 토큰 재사용 401(`WWW-Authenticate: Bearer`)·최신 토큰 401·그 사용자 행 0개, 빈 `refresh_token` 400 `errors[0].field = refresh_token`, DB에는 16진수 64자 해시만(원문 0건), **서버 재시작 뒤 옛 액세스 토큰 401 → 재발급 200 → `/users/me` 200·TODO 생성 201**, 로그에 토큰·비밀번호·ERROR 0건
- 사용자 질문(TTL 설정 위치): yml 값 + `@Value` 키 참조(현행) vs `@Value` 기본값 비교 — 현행 유지 권장(인증 정책을 yml 한 곳에서 확인, 키 누락·오타 시 기동 실패로 즉시 발견 — 사본에서 키 삭제 시 `Could not resolve placeholder 'auth.access-token-ttl'` 실측, Spring Boot 문서상 `@Value`는 메타데이터 없음·relaxed binding 제한, 그룹 키는 `@ConfigurationProperties` 권장이나 D-03과 충돌). 결정은 사용자 확인 대기
- 독립 리뷰(`step-reviewer`): 코드 지적 없음 — 필수 테스트 파일 4개 diff 없음, `noRollbackFor`가 효과 있는 구조(예외를 메서드 안에서 직접 던지고 다른 트랜잭션 경계 없음), 만료 판정·정리 기준 일치, 원문 미저장·미로그, 풀 1개에서 두 번째 커넥션 요구 없음, D-20 네이밍·의존 방향 준수, `./gradlew test` 두 번 연속 17건 통과. 문서 지적 4건 반영: SEC-01 문구(이제 토큰 쌍 반환), API-02 로그인 실패 401 근거(재확인), SEC-11 7일 근거(재확인), DB-06 DDL 그대로 전사. 참고: 로그인은 같은 트랜잭션에서 리프레시 토큰도 저장하므로 저장이 실패하면 로그인도 실패한다(D-15 설계의 결과 — 세 테이블이 같은 ddl-auto로 함께 생겨 채점 환경에서 일어날 현실적 조건은 없음)
  - 재확인(`step8-recheck.txt`): 틀린 비밀번호·없는 사용자 → 같은 401·같은 detail·`WWW-Authenticate: Bearer`, 리프레시 토큰 `expires_at` = 발급 시각 + 604,799,884~604,800,142ms(7일 = 604,800,000ms), 로그에 토큰·비밀번호 0건
- 다음: 사용자 확인(TTL 설정 위치 결정 포함) → 9단계

### 2026-10-26 · S11 테이블 생성 시점 확인, JDK 실행 범위 점검(JDK 27 실격 위험)
- 질문: 테이블이 서버 기동 때 생기는지, README 명령을 해야 생기는지 → 기동 때 자동 생성(실행 방법과 무관), README `bootRun`이 곧 기동
- 실행 명령과 결과(스크래치 `table-creation/report.txt`):
  - A `todo.db` 없는 상태 `bootRun`(SQL 로그 켬) — 요청 0건. `create table todos`·`users`·`create index`가 HikariPool 시작 뒤, Tomcat 시작 전에 실행(09:09:45.99 → Tomcat 09:09:47.43), 기동 완료 시 스키마 확인
  - B 같은 DB로 재기동 → DDL 0건, users 1행 유지, 기존 계정 로그인 200
  - C 다른 폴더에서 `java -jar` → 그 폴더에 `todo.db`와 테이블 생성(프로젝트 루트에는 없음 — DB 위치는 실행 위치)
  - D `./gradlew cleanTest test` 통과, 테스트 DB는 끝난 뒤 비어 있음(create-drop), `todo.db` 없음
  - SQL 로그의 `alter table users drop constraint …`는 Hibernate 기본 전략(`DROP_RECREATE_QUIETLY`)이 시도하고 SQLite가 지원하지 않아 조용히 무시된다(6.6.53 소스). 경고·오류 없음, `unique` 유지
- 사용자 지시 "실격 사유 절대 불허" → 실행 가능한 JDK 범위 점검. 공식 문서: Gradle 9.8.2는 JDK 17~27, Lombok 1.18.46은 ~26(27은 1.18.48), Boot 3.5.16은 공식 호환 ~25
- 실측(스크래치 `jdk-matrix/report.txt`·`report-fix.txt`): 커밋본 `17903c7`을 `채점 환경/홍길동_springboot/assignment`(한글·공백 경로)에 새로 풀어 실행. JDK는 공식 빌드를 받아 SHA-256 확인(Temurin 25.0.5·26.0.2.1, Corretto 27.0.1 — 시스템 설치 없이 스크래치에 압축 해제)
  - JDK 17·25·26: `clean build` 14건 통과, `bootRun` 기동 직후 테이블 3개, API(가입·로그인·TODO·401) 정상
  - JDK 27: `compileJava` 실패 — `ClassNotFoundException: com.sun.tools.javac.tree.EndPosTable`(Lombok 1.18.46)
  - 사본에만 `ext['lombok.version'] = '1.18.48'` 적용 → JDK 17·21·25·26·27 모두 위 항목 통과. JDK 25 이상은 sqlite-jdbc 네이티브 로드 경고 4줄(JEP 472, 향후 차단 예고)만 있음
- 그 밖: 프로젝트 루트에 `memory-bank/`(템플릿과 파일 목록, 09:22 생성 — 이 세션이 만들지 않음)가 다시 생김 → 커밋하지 않고 협의. "statusline 은 실행 취소" 지시는 과제와 무관(used_prompts 기록 대상 아님) — 이 세션에서 실행한 statusline 작업은 없고, 전역 설정 `statusLine` 변경(09:18)도 이 세션이 한 것이 아님 → 처리 여부 협의
- 실수: JDK 다운로드 스크립트가 체크섬 빈 행에서 경로를 잘못 만들어 실패 → 공식 SHA-256과 대조해 다시 받음
- 다음: 협의(Lombok 1.18.48 반영, 교차 확인 JDK 범위, Windows 실측 방법, README 항목, 테이블 생성 문구, `memory-bank/`) → 합의 후 8단계

### 2026-10-25 · S10 명세 재대조, 7단계 TODO 기능(TEST-02·04·X2), 계층 DTO·네이밍 정리(D-20)
- 명세 재대조: Notion 원문을 다시 받아 로컬 사본과 비교 — 블록 76개 동일, 최종 수정 2026-09-30. 누락·오류 없음. 보강 7건: TEST-X2를 7단계로(API-09는 명세 필수 동작), TEST-04를 GET·PUT·DELETE로, SQLite ID 재사용 문서화(R-7), R-3 실측, 8단계 재사용 탐지 롤백 함정(`noRollbackFor`, D-15), ZIP `gradlew` 실행 권한·`settings.gradle` 확인(R-8, SKILL), OPT-03 판단 항목(D-14)
- 사용자 추가 지시(작업 중): Result → Response 변환(웹 ↔ 도메인 결합 제거, SOLID), 참고 프로젝트 네이밍 → D-20. user 기능도 같은 방식으로 정리하고, CLAUDE.md를 압축할 때 빠진 초안 §5.7 "SOLID"를 원칙 6에 복원. 참고 프로젝트와 다른 점 2가지(Request/Response는 package-private 유지를 위해 `web`, JPA 엔티티는 `JpaEntity`)
- 구현: todo 기능(도메인 `Todo`, `TodoUseCase`·`TodoService`, `CreateTodoCommand`·`UpdateTodoCommand`·`TodoResult`, `TodoCommandPort`·`TodoQueryPort`, `TodoController`·Request 2개·`TodoResponse`, `TodoJpaEntity`·`TodoRepository`·`TodoRepositoryAdapter`), `TodoNotFoundException` 404 매핑, user 기능 리팩터링(`SignupCommand`·`LoginCommand`·`UserResult`·`TokenResult`, `UserCommandPort`·`UserQueryPort`·`PasswordHashPort`·`AccessTokenPort`, `UserRepositoryAdapter`·`PasswordHashAdapter`·`AccessTokenAdapter`), 테스트 `TodoCrudFlowTest`(TEST-02)·`TodoNotFoundTest`(TEST-04)·`TodoOwnershipTest`(TEST-X2)·`IntegrationTestSupport.signupAndLogin()`
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → 14건 통과(첫 실행), 리팩터링 뒤 14건 통과. 정적 점검: web 어댑터의 domain import 0건, application의 웹·JPA·Security import 0건, 도메인 순수성 0건
  - 변이 확인(되돌린 뒤 `cmp`로 원본과 일치 확인): 소유자 조건 제거 → TEST-X2만 실패(필수 4종은 통과 — X2를 앞당긴 근거), 삭제 204→200 → TEST-02 실패, Result 변환에서 제목·설명 뒤바꿈 → TEST-02·X2 실패
  - JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=14 failures=0, `todo.db` 없음
  - bootRun + curl(JDK 17, `todo.db` 없는 상태에서 시작): 스키마 `todos(id integer, completed boolean not null, description varchar(1000), title varchar(100) not null, user_id bigint not null, primary key (id))` + `CREATE INDEX idx_todos_user_id on todos (user_id)`, `users(username varchar(50) not null unique)`. 흐름 201 + `Location: /todos/1` → 목록 200 → 단건 200 → 수정 200 → 삭제 204(본문 0바이트) → 단건 404 ProblemDetail·목록 `[]`. 오류: 빈 제목·101자 제목·1,001자 설명·PUT `completed` 누락 → 400(`errors`), `completed` 문자열·본문 없음·깨진 JSON → 400(`errors` 없음, 프레임워크 기본 detail "Failed to read request"), `text/plain` 415·PATCH 405(둘 다 `application/problem+json`), 숫자 아닌 ID 400, 토큰 없음 401. 소유권: 타인 GET·PUT·DELETE 404, 타인 목록에 없음, 본문에 A의 `userId`를 넣은 B의 요청은 B 소유로 저장(sqlite3로 `user_id` 확인). ID 재사용 재현(A가 지운 4를 B의 새 TODO가 받음, A가 조회하면 404). 동시 요청(TODO API 기준) POST 60건·GET 60건(병렬 30) 모두 성공, 행 +60, 오류 로그 없음 → R-3 해소. `/v3/api-docs`에 `@CurrentUserId` 미노출. 로그에 토큰·비밀번호 0건
  - 독립 리뷰(`step-reviewer`): 높음·중간 지적 없음. TODO 기능·TEST-02·04·X2·D-20 리팩터링이 명세·`api.md`·rules와 일치, D-20의 참고 프로젝트와 다른 점 2가지는 타당하다고 판단. 테스트 재실행 14건 통과. 낮음 지적 반영: ① 400 서술 정정(위 문구 — 응답 본문을 다시 받아 `step7-recheck2.txt`로 저장), ② SEC-08·D-10 상태 갱신, ③ 8단계 함정 문구 보완(예외가 다른 `@Transactional` 경계를 거치면 rollback-only로 500, `REQUIRES_NEW`는 무한 정지가 아니라 connectionTimeout 30초 뒤 실패·풀을 늘려도 SQLite 잠금으로 실패). 관찰: 가입·로그인은 bcrypt를 트랜잭션 안에서 실행해 커넥션 1개를 쥐므로 로그인이 몰리면 다른 요청이 기다린다(채점 영향 없음, D-10)
  - 지침 로드 기록: 이번 압축 직후 `compact` 이유로 CLAUDE.md 5,753자·해시 `6a1435fe2e14` 기록 = 커밋본과 일치(D-17·DOC-08)
- 실수·정정: 스모크 출력에서 한 줄(설명 1,001자)이 두 번 찍혔다 → 같은 요청을 단독으로 다시 보내 응답 1건(400, `description` 오류)·연결 1회를 확인
- 다음: 사용자 확인 후 8단계

### 2026-10-25 · S9 6단계 설계 재검토와 User·Auth(TEST-01)
- 설계 재검토(구현 전 보고): 로그인은 필수 여부만 검증(bcrypt 72바이트 예외는 인코딩에서만 — 6.5.11 소스), 검증 메시지 한국어 고정(로캘 의존 제거), `PasswordEncoder` 빈 없음(어댑터 내부), 어댑터 클래스 package-private, 사용자 없는 `/users/me` 404 — D-03·D-09·rules 반영
- 구현: `user` 기능(도메인 `User`, 포트 `UserUseCase`·`AuthUseCase`·`UserRepository`·`PasswordHasher`·`TokenIssuer`, 서비스 2개, 예외 3개, 컨트롤러 2개·요청/응답 record·`@MaxUtf8Bytes`, JPA 엔티티·리포지토리·영속성 어댑터, `DelegatingPasswordHasher`·`JwtTokenIssuer`), `@CurrentUserId`, `JwtEncoder` 빈, `auth.access-token-ttl: 1h`, 검증 오류 `errors`·기능 예외 매핑, `UserAuthFlowTest`(TEST-01)
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → 9건 통과(첫 실행에 통과), 도메인 순수성 grep 0건
  - JDK 17·21 `./gradlew clean build` → BUILD SUCCESSFUL, tests=9 failures=0, `todo.db` 없음
  - bootRun + curl(JDK 17): 가입 201 `{id, username}`, 중복 409, 짧은 비밀번호·한글 31자(93바이트)·빈/51자 username → 400 `errors`(중복 항목 없음), 잘못된 JSON 400(프레임워크 기본 detail), 로그인 200 `{access_token}`만, 틀린 비밀번호·없는 사용자·72바이트 초과 비밀번호 → 같은 401 + `WWW-Authenticate: Bearer`, 빈 비밀번호 400, `/users/me` 200, 토큰 헤더 HS256·클레임 `exp·iat·sub`·`exp-iat=3600`, DB 저장값 `{bcrypt}$2a$10$…`(68자), `users` 스키마 `username varchar(50) not null unique`, 사용자 삭제 뒤 같은 토큰 404, 로그에 비밀번호·토큰 0건
  - 독립 리뷰(`step-reviewer`): 지적 없음. 설계 선택 5개 모두 타당(bcrypt 경로를 바이트코드로 재확인). 문서 정합(API-03 404, 단계 문구) 반영. TEST-01만으로는 다중 사용자 분리를 증명하지 못한다는 주의 — 7단계 TEST-02·04와 10단계 TEST-X2로 확인
- 다음: 사용자 확인 후 7단계

### 2026-10-25 · S1~S8 요약 (세부는 이 파일의 git 이력)
- S1 설계 검증과 작업 체계 수립: CLAUDE.md 초안 458줄 → 72줄, rules·skill·docs 작성. Boot 4.1.2·Gradle 9.8.2 생성물, SQLite FK DDL 미생성, BCrypt 72바이트 예외, NimbusJwtEncoder 기본 RS256, ERROR 디스패치 401 함정 확인
- S2 명세 원문 수집(블록 76개)·대조, 사용자 결정 3건(빌드 기준선·Java 17 타깃·springdoc 유지), `memory-bank/` 백업 후 삭제, `git init`
- S3 저장소 정리·원격 연결, 테이블 생성 경로(D-10)·재발급 확장(D-15) 설계
- S4 빌드 기준선 교정(Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17), 커밋 작성자를 닉네임 + noreply로 재작성, 프롬프트 기록 훅·백필(D-16)
- S5 저장소 재생성 확인, 푸시는 저장소 로컬 헬퍼(`gh auth token --user manners-parkpro`), JDK 17 기본 + 21 교차 확인(사용자 결정)
- S6 4단계 설정·테스트 기반(SQLite·풀 1·`ddl-auto: update`, 테스트 DB `build/todo-test.db`). springdoc 문서 엔드포인트 기본 활성 WARN 2건은 9단계에서 처리 검토
- S7 압축 뒤 프롬프트 기록·CLAUDE.md 로드 검증, 훅 추가(`log-instructions.py`·`guard-git.py`), `step-reviewer`, 설계 문서 리팩터링(독립 리뷰·best practices 대조)
- S8 사용자 참조 이름 `userId` 결정, 5단계 보안 골격(TEST-03) — ProblemDetail 엔트리 포인트, 공개 인증 경로 Bearer 헤더 무시, 기본 `logout` 필터 비활성(리뷰 지적 재현 후)
