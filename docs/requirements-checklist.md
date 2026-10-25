# 요구사항 체크리스트

> 출처: **명세** = 과제 명세 원문(절 이름 표기) · **초안** = 사용자 초안 지침의 필수 조건 · **요청** = 이후 사용자 추가 요청 · **설계** = 명세가 정하지 않아 결정한 사항(`docs/decisions.md`)
> 명세 원문 사본: `docs/assignment-spec.md`(로컬 전용, 공개 저장소 제외) — 출처 https://chalk-mitten-2ee.notion.site/JWT-TODO-API-3db5808f6b8d805f8ad9e32a54cd66e6
> 상태: ⬜ 미착수 · 🟨 진행 중 · ✅ 검증 완료 · ❌ 불일치·실패 · ⏸ 보류 · — 해당 없음
> 규칙: 항목을 삭제하지 않는다. ✅는 실제 실행 근거(명령·결과)가 `docs/progress.md`에 있을 때만 표시한다. **⚠ 실격**은 명세의 실격 조건이다.
> 평가 기준(명세): 요구사항대로 동작 · 코드 가독성 · 테스트가 핵심 흐름 검증 · AI 활용과 검증

## 1. 기술·실행 (TECH)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| TECH-01 | Java 17 이상 — 컴파일 대상 17(`release 17`), JDK 17·21에서 빌드 | 명세 기술 조건·D-01 | `build.gradle` | JDK 17·21 `./gradlew test`, class major 61 | ✅ JDK 17·21에서 `clean build` 통과(S7), class 61(S4) |
| TECH-02 | Spring Boot 3.5.16 | 초안·D-01 | `build.gradle` | `./gradlew dependencies` | ✅ spring-boot 3.5.16 해석 확인(S4) |
| TECH-03 | Gradle Wrapper 포함, 9.8.2 유지(Boot 3.5 조합 실측) | 명세 기술 조건·D-01 | `gradlew`, `gradle/wrapper/` | `./gradlew --version`, `--warning-mode all` | ✅ Gradle 9.8.2 + Boot 3.5.16, JDK 17·21에서 `clean build` 통과(S7). deprecation 경고 1건(Gradle 11 제거 예정) |
| TECH-04 | ORM은 Spring Data JPA(명세 권장), DB는 SQLite3 | 명세 기술 조건 | `build.gradle`, `application.yaml` | 기동·CRUD 테스트 | 🟨 bootRun 기동·SQLite 3.49.1 인식(S6) — CRUD는 7단계 |
| TECH-05 | 테스트는 JUnit 5 (Boot 3.5.16 → 5.12.2) | 명세 기술 조건 | `src/test` | `./gradlew test`, 의존성 버전 | ✅ JUnit 5.12.2로 테스트 실행(S6) |
| TECH-06 | 테이블은 README 명령만으로 생성 — 생성 경로는 `ddl-auto` 하나(수동 SQL·`schema.sql`·DB 파일 제공 금지), README에 SQL 명령 없음 | 명세 기술 조건·요청·D-10 | `application.yaml` | `todo.db`를 지운 클린 상태에서 README 명령만 실행 후 `.schema` | ⬜ |
| TECH-07 | **⚠ 실격** README에 적힌 명령어로 실행·테스트 가능 | 명세 실격 조건 | `README.md` | README 순서대로 클린 실행 | ⬜ |
| TECH-08 | 내장 Tomcat 보안 패치 버전(10.1.61) 오버라이드 | 설계·D-01 | `build.gradle` | `dependencyInsight` | ✅ 10.1.55 → 10.1.61 selected by rule(S4) |

## 2. API (API) — 계약 상세: `.claude/rules/api.md`
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| API-01 | `POST /users/signup` 회원가입 | 명세 구현 API | user | TEST-01 | ✅ 201 `{id, username}`(TEST-01, S9). 중복 409·검증 400 실측(S9 bootRun) |
| API-02 | `POST /users/login` 로그인, `access_token` 반환(+ 확장 필드 `refresh_token`) | 명세 구현 API·요청·D-15 | user(Auth) | TEST-01 | 🟨 필수 범위 완료 — 200 `{access_token}`, 실패 401(TEST-01·S9 bootRun). 확장 필드 `refresh_token`은 8단계 |
| API-03 | `GET /users/me` 내 정보 조회 (인증) | 명세 구현 API | user | TEST-01 | ✅ 토큰으로 200 `{id, username}`(TEST-01, S9), 삭제된 사용자 404(S9 bootRun) |
| API-04 | `POST /todos` 생성 (인증) | 명세 구현 API | todo | TEST-02 | ⬜ |
| API-05 | `GET /todos` 내 목록 조회 | 명세 구현 API | todo | TEST-02 | ⬜ |
| API-06 | `GET /todos/{id}` 단건 조회 | 명세 구현 API | todo | TEST-02·04 | ⬜ |
| API-07 | `PUT /todos/{id}` 수정 | 명세 구현 API | todo | TEST-02 | ⬜ |
| API-08 | `DELETE /todos/{id}` 삭제 | 명세 구현 API | todo | TEST-02 | ⬜ |
| API-09 | 로그인한 사용자 본인의 TODO만 다룬다 — 타인 TODO는 404로 은닉 | 명세 구현 API·D-08 | todo service·persistence | TEST-X2 | ⬜ |
| API-10 | API 문서(springdoc Swagger UI) — 보호 API에만 Bearer 요구 표시 | 설계·D-02 | `global/config`, 컨트롤러 | `/v3/api-docs` 확인 | ⬜ |
| API-11 | `POST /users/refresh` 리프레시 토큰으로 새 토큰 쌍 재발급 — 액세스 토큰 만료·서버 재시작 후 API 재호출 | 요청·D-15 | user(Auth) | TEST-07·08 | ⬜ |

## 3. 인증·보안 (SEC)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| SEC-01 | 로그인 성공 시 `{ "access_token": "..." }` 포함 | 명세 인증 | user web | TEST-01 | ✅ 응답 필드가 `access_token` 하나(TEST-01, S9 bootRun) |
| SEC-02 | 인증 헤더 `Authorization: Bearer <token>` | 명세 인증 | `global.security` | TEST-01·03 | ✅ 유효 토큰으로 `/users/me` 200(TEST-01, S9), 헤더 없음·형식 오류 401(TEST-03, S8) |
| SEC-03 | 액세스 토큰 만료 시간(1시간)·README 명시 (Refresh Token은 명세상 선택 → SEC-11로 구현) | 명세 인증·초안·D-06 | `auth.access-token-ttl` | 설정·README | 🟨 `exp - iat = 3600`초, 헤더 HS256, 클레임 `sub·iat·exp`뿐(S9 bootRun). README는 12단계 |
| SEC-04 | 토큰 없이 요청하면 401 | 명세 테스트 | `global.security` | TEST-03 | ✅ 보호 경로 6개(핸들러는 6·7단계) 토큰 없음 401(TEST-03, S8 JDK 17·21) |
| SEC-05 | 무효·만료 토큰 → 401 (clock skew 0) | 초안·D-05 | `JwtDecoder` | TEST-X1, TEST-07 | ⬜ |
| SEC-06 | 비밀번호 평문 저장 금지 (DelegatingPasswordEncoder) | 초안·D-09 | `PasswordHasher` 어댑터 | DB 값이 `{bcrypt}` 접두 해시인지 확인 | ✅ 저장값 `{bcrypt}$2a$10$…`(68자, S9 sqlite3) |
| SEC-07 | 서명 키 하드코딩 금지(미설정 시 임의 키 생성)·README 설명 | 초안·D-06 | `application.yaml`, JWT 설정 | grep 점검, README | 🟨 `JwtConfig` — 미설정 시 임의 키+WARN, Base64 아님·32바이트 미만은 기동 실패, 로그에 키 값 없음(S8 bootRun). README 설명은 12단계 |
| SEC-08 | 인증 로직은 공통 보안 구성 (컨트롤러별 중복 금지) | 초안 | `SecurityFilterChain` | 코드 리뷰 | 🟨 `SecurityConfig` 하나로 구성(S8), user 컨트롤러는 `@CurrentUserId`만 사용(S9). todo는 7단계 |
| SEC-09 | 응답·예외·로그에 비밀번호·키·토큰·민감정보 비노출 | 초안 | 전역 | 응답·로그 확인 | 🟨 user 응답에 비밀번호 없음(TEST-01), 실행 로그에 비밀번호·토큰 없음(S9 bootRun). 최종 점검은 13단계 |
| SEC-10 | OSS 종료 계열 CVE의 전제 기능 미사용 | 설계·D-01 | 전역 | grep 점검(submission-check) | ⬜ |
| SEC-11 | 리프레시 토큰: 불투명 32바이트·SHA-256 해시 저장·7일 만료·회전·재사용 탐지 | 요청·D-15 | user(Auth), `refresh_tokens` | TEST-08, DB에 원문 없음 확인 | ⬜ |
| SEC-12 | 공개 인증 엔드포인트는 `Authorization` 헤더를 무시(만료 토큰을 단 재발급 허용) | 설계·D-15 | `BearerTokenResolver` | TEST-07 | 🟨 가입·로그인 경로는 엉터리 Bearer 헤더에도 401이 아님(S8 bootRun — 미구현 경로라 404). `/users/refresh`는 8단계, TEST-07 |

## 4. 상태 코드·예외 (ERR)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| ERR-01 | 인증 없이 보호 API 호출 → 401 | 명세 테스트 | `global.security` | TEST-03 | ✅ ProblemDetail 401 + `WWW-Authenticate: Bearer`(TEST-03, S8) |
| ERR-02 | 존재하지 않는 TODO ID → 404 | 명세 테스트 | todo, `global.error` | TEST-04 | ⬜ |
| ERR-03 | 성공 코드: 생성 201 · 조회 200 · 수정 200 · 삭제 204 | 초안·D-07 | 컨트롤러 | TEST-01·02 | 🟨 가입 201·로그인 200·내 정보 200(TEST-01, S9). TODO는 7단계 |
| ERR-04 | 명세 외 응답 형식·상태 코드의 일관성(ProblemDetail, 400·409) | 초안·D-07 | `global.error` | TEST-X3·X4 | 🟨 401·404가 ProblemDetail로 응답(S8). 400·409는 6·7단계, 검증은 10단계 |

## 5. DB (DB)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| DB-01 | `sqlite-jdbc`·`hibernate-community-dialects` 의존성, dialect `SQLiteDialect`, `ddl-auto: update` | 명세 SQLite 설정 참고 | `build.gradle`, `application.yaml` | 기동 로그, `./gradlew dependencies` | ✅ 의존성·dialect·`ddl-auto: update` 설정, bootRun 기동(S4·S6) |
| DB-02 | SQLite에서 생성·조회·수정·삭제 실제 동작 | 초안 | persistence 어댑터 | TEST-02 | ⬜ |
| DB-03 | 테이블·관계(FK 미생성 대응): `users`(username `unique`), `todos`(`user_id` 인덱스) — 7단계 | 초안·D-04·D-10 | JPA 엔티티 | `sqlite3 todo.db '.schema'` | 🟨 `users`(username `varchar(50) not null unique`) 생성 확인(S9). `todos`는 7단계 |
| DB-04 | 개발 DB와 테스트 DB 분리 | 초안·D-10 | `todo.db` / `build/todo-test.db` | 테스트 후 `todo.db` 미변경 | ✅ 테스트는 `build/todo-test.db` 사용, 실행 후 `todo.db` 미생성(S6, JDK 17·21) |
| DB-05 | `*.db` 커밋·제출 제외 | 명세 제출물 | `.gitignore`, ZIP | `git check-ignore`, `unzip -l` | 🟨 `.gitignore` 검증 완료(2026-10-25), ZIP 미검증 |
| DB-06 | `refresh_tokens`(`token_hash` `unique`, `user_id` 인덱스) — 8단계(S7에 DB-03에서 분리) | 요청·D-15 | JPA 엔티티 | `sqlite3 todo.db '.schema'` | ⬜ |

## 6. 테스트 (TEST)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| TEST-01 | 회원가입 → 로그인 → 발급받은 토큰으로 `/users/me` 조회 성공 | 명세 테스트 | `src/test` | `./gradlew test` | ✅ `UserAuthFlowTest` — 가입 201 → 로그인 `access_token`만 사용 → `/users/me` 200·id·username 일치·비밀번호 없음(S9, JDK 17·21) |
| TEST-02 | TODO 생성 → 목록 조회 → 수정 → 삭제 | 명세 테스트 | `src/test` | `./gradlew test` | ⬜ |
| TEST-03 | 토큰 없이 요청하면 401 (가능하면 무효 토큰도 확인) | 명세 테스트·초안 | `src/test` | `./gradlew test` | ✅ `AuthenticationRequiredTest` — 보호 경로 6개(핸들러는 6·7단계) 토큰 없음·형식 오류 토큰 401, ProblemDetail·`WWW-Authenticate` 검증(S8, JDK 17·21 7건 통과) |
| TEST-04 | 존재하지 않는 TODO ID로 요청하면 404 | 명세 테스트 | `src/test` | `./gradlew test` | ⬜ |
| TEST-05 | `./gradlew test` 명령으로 실행 | 명세 테스트 | `build.gradle` | 실행 로그 | 🟨 `./gradlew test` 실행 확인(S9: 9건 통과 — contextLoads·TEST-01·TEST-03) — TEST-02·04는 7단계 |
| TEST-06 | 반복 실행 일관·테스트 간 데이터 독립·`./gradlew build` 확인 | 초안 | 테스트 구성 | 2회 연속 실행, `build` | ⬜ |
| TEST-07 | 액세스 토큰 만료 → 보호 API 401 → `/users/refresh`(만료 토큰이 헤더에 있어도) 200 → 새 토큰으로 같은 API 200 | 요청·D-15 | `src/test` | `./gradlew test` | ⬜ |
| TEST-08 | 리프레시 토큰 회전·재사용 탐지 — 사용한 토큰 재사용 401, 이후 최신 토큰도 401 | 요청·D-15 | `src/test` | `./gradlew test` | ⬜ |
| TEST-X1 | (선택) 위조 서명·만료 토큰 → 401 | 명세 선택 사항·설계 | `src/test` | `./gradlew test` | ⬜ |
| TEST-X2 | (선택) 타인 TODO 조회·수정·삭제 → 404 | 명세 선택 사항·설계 | `src/test` | `./gradlew test` | ⬜ |
| TEST-X3 | (선택) username 중복 가입 → 409 | 명세 선택 사항·설계 | `src/test` | `./gradlew test` | ⬜ |
| TEST-X4 | (선택) 입력 검증 실패 → 400 | 명세 선택 사항·설계 | `src/test` | `./gradlew test` | ⬜ |

필수 시나리오 단계 — 4종을 먼저 완성한 뒤 TEST-07·08, TEST-X를 작성한다. 필수 4종은 확장 기능(Refresh Token)과 독립이며, 확장 구현 후 회귀를 확인한다(D-15).
- **TEST-01**: 가입 → 로그인 → 응답의 `access_token` 추출 → `Authorization: Bearer <token>`으로 `GET /users/me` → 성공 상태와 사용자 정보 검증
- **TEST-02**: 사용자 준비·로그인 → TODO 생성 → 목록에 포함 확인 → 수정 → 수정값 반영 확인 → 삭제 → 삭제 결과 확인(단건 404·목록 미포함)
- **TEST-03**: 보호 API를 Authorization 헤더 없이 호출 → 401
- **TEST-04**: 인증된 요청으로 존재하지 않는 ID 단건 조회 → 404

## 7. 문서·제출 (DOC·SUB)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| DOC-01 | README: 선택한 스택(B. Spring Boot와 버전) | 명세 제출물 | `README.md` | 검토 | ⬜ |
| DOC-02 | README: 실행 방법(JDK 17 이상, 실행 명령만으로 테이블 생성, `JWT_SECRET` 선택, Windows 명령 병기, SQL 명령 없음) | 명세 제출물·초안·요청 | `README.md` | README대로 실행 | ⬜ |
| DOC-03 | README: 테스트 실행 방법 `./gradlew test` | 명세 제출물 | `README.md` | 실행 확인 | ⬜ |
| DOC-04 | README: API 목록 요약(+ Swagger UI 주소) | 명세 제출물·D-02 | `README.md` | `api.md`와 대조 | ⬜ |
| DOC-05 | README: 인증 방법(로그인 → Bearer, 만료 1시간, `/users/refresh` 재발급) | 초안·요청 | `README.md` | 검토 | ⬜ |
| DOC-06 | **⚠ 실격** `prompts/used_prompts.md` — 주요 프롬프트와 결과 확인·수정 내용을 간단히, 링크 대체 불가 | 명세 제출물·실격 조건 | `prompts/used_prompts.md` | 제출 전 내용 확인 | 🟨 #1~#6 기록 — 압축 뒤 원문 대조로 누락 없음 확인(S7) |
| DOC-07 | 프롬프트 원문 보존 — 받은 턴 안에 기록, 훅 자동 백업(로컬), 이전 프롬프트 백필 | 요청·D-16 | `.claude/hooks/log-prompt.py`, `.claude/prompt-history.jsonl` | 파이프 테스트, 다음 프롬프트 기록 확인 | ✅ 실제 프롬프트·작업 중 메시지가 `source: "hook"`으로 기록됨(S5), 압축 뒤 첫 프롬프트 기록·하네스 메시지 제외(S7) |
| DOC-08 | 세션 시작·압축 후 CLAUDE.md 전체 로드 확인과 지침 로드 기록 | 요청·D-17 | `.claude/hooks/log-instructions.py`, `.claude/instructions-log.jsonl` | 트랜스크립트 주입본과 디스크 파일 비교, 훅 기록 | 🟨 CLAUDE.md 전체 주입·`path_glob_match` 기록 확인(S7). `session_start`·`compact` 기록은 다음 세션·압축 때 확인 |
| SUB-01 | 파일명 `이름_springboot.zip`, 제출처 채용 사이트(사람인/잡코리아) | 명세 제출물 | — | 파일명 확인 | ⬜ |
| SUB-02 | 제외: `build/`, `target/`, `.gradle/`, `*.db` | 명세 제출물 | ZIP | `unzip -l` | ⬜ |
| SUB-03 | 포함: 소스 전체 — `src/`, `build.gradle`, `README.md`, `prompts/used_prompts.md` | 명세 제출물 | ZIP | `unzip -l` | ⬜ |
| SUB-04 | Wrapper(`gradlew`, `gradle/wrapper/*`) 포함, `.claude/settings.local.json`·`.claude/*.jsonl`(로컬 기록)·`HELP.md` 제외 | 설계·D-13·D-16 | ZIP | `unzip -l` | 🟨 `HELP.md` 삭제 완료, ZIP 미검증 |
| SUB-05 | 공개 저장소에는 명세 사본 제외(`.gitignore`), 제출 ZIP에는 포함 | 요청·D-13 | `.gitignore`, ZIP | `git check-ignore`, `git ls-tree`, `unzip -l` | 🟨 원격 트리 제외 확인(S7, `origin/main`), ZIP 미검증 |
| SUB-06 | 커밋 작성자 정보에 회사 도메인 없음(닉네임 + GitHub noreply) | 요청·D-13·D-19 | 저장소 로컬 git 설정, `guard-git.py` 훅 | `git log --format='%ae %ce'` | ✅ 원격 `main`의 모든 커밋 작성자·커미터가 noreply(S7), `manners-parkpro` 연결(S5, GitHub API). 커밋·푸시 훅이 강제(S7) |

## 8. 선택 사항 — 가산점 (OPT, 필수 완료 후 결정 — D-14)
| ID | 요구사항 | 출처 | 구현 위치 | 검증 방법 | 상태 |
|---|---|---|---|---|---|
| OPT-01 | `GET /todos/search?keyword=` 제목으로 검색 | 명세 선택 사항 | — | — | ⏸ |
| OPT-02 | `PUT /users/me` 회원정보 수정 (비밀번호 변경 시 리프레시 토큰 폐기 필요) | 명세 선택 사항 | — | — | ⏸ |
| OPT-03 | `DELETE /users/me` 탈퇴 (TODO·리프레시 토큰 정리 필요) | 명세 선택 사항 | — | — | ⏸ |

## 9. 미해결·위험
| ID | 내용 | 조치 |
|---|---|---|
| R-1 | Boot 3.5.16 + Gradle 9.8.2는 공식 지원 목록 밖 | 해결: JDK 17·21에서 `clean build` 통과(S7) — 폴백 불필요 |
| R-2 | Boot 3.5 OSS 종료 — 엔터프라이즈 전용으로만 수정된 CVE 존재(전제 기능 미사용으로 비해당) | SEC-10 유지, Tomcat은 OSS 패치 반영(TECH-08) |
| R-3 | Hikari 풀 1개 설정의 실제 동작 미검증 | 기동·contextLoads에서 연결 문제 없음(S6). 요청 처리 중 동작은 7단계 이후 확인 |
| R-4 | springdoc 스키마와 실제 응답 표기 일치 여부 | `/v3/api-docs`에서 `access_token`·`refresh_token` 확인 |
| R-5 | 원격 저장소가 공개 — 비밀·명세 사본이 올라가면 안 됨 | 커밋 전 `git status`·`check-ignore` 확인, 비밀은 저장소에 두지 않음(SEC-07) |
| R-6 | 원격에 회사 도메인 이메일이 담긴 커밋 `52809a9`가 남아 있음. force push 뒤에도 GitHub는 SHA로 접근 가능한 캐시를 남길 수 있음(GitHub 문서) | 해결(S5): 저장소 재생성, 이전 커밋 조회 시 HTTP 409 |

해결됨(2026-10-25): 원문 명세 확보·대조, Lombok·springdoc 유지 결정, `memory-bank/` 삭제, Git 저장소 초기화와 원격 연결.
