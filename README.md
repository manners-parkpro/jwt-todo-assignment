# JWT TODO API

JWT 인증이 적용된 TODO REST API다. 회원가입·로그인으로 받은 액세스 토큰으로 **자기 TODO만** 생성·조회·수정·삭제한다.

- 과제의 필수 API 8개와 필수 테스트 4종을 구현했다. 여기에 액세스 토큰 재발급(리프레시 토큰)과 선택 사항(제목 검색, 회원정보 수정·탈퇴, 추가 테스트)을 더했다.
- 이 문서의 명령은 macOS에서 직접 실행해 확인했다. GitHub Actions(`.github/workflows/ci.yml`)로도 Gradle 명령은 Windows·Linux·macOS × JDK 17·21에서, curl 예시는 Linux·macOS에서 실행해 확인한다.

## 선택한 스택 — B. Spring Boot

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 17 이상 (컴파일 대상 Java 17, JDK 17·21에서 확인) |
| 프레임워크 | Spring Boot 3.5.16 (Spring Web · Validation · Security) |
| 빌드 | Gradle 9.8.2 — Gradle Wrapper 포함, 따로 설치하지 않아도 된다 |
| ORM | Spring Data JPA (Hibernate 6.6) |
| DB | SQLite3 — `org.xerial:sqlite-jdbc`, `hibernate-community-dialects`의 `SQLiteDialect`, `ddl-auto: update` |
| 인증 | JWT(HS256) — Spring Security OAuth2 Resource Server |
| 테스트 | JUnit 5, Spring Boot Test(MockMvc 통합 테스트) |
| API 문서 | springdoc-openapi (Swagger UI) |

## 실행 방법

모든 명령은 프로젝트 루트(이 README와 `gradlew`가 있는 폴더)에서 실행한다.

### 준비
- **JDK 17 이상**만 있으면 된다. Gradle은 설치하지 않아도 된다. 처음 실행할 때 Gradle Wrapper가 Gradle 9.8.2와 의존성을 내려받는다. 그래서 인터넷 연결이 필요하고, 첫 실행은 더 오래 걸린다.
- Gradle은 `JAVA_HOME`의 JDK로 실행된다. `JAVA_HOME`이 없으면 PATH의 `java`를 쓴다. 어떤 JDK가 쓰이는지는 `./gradlew --version`의 `Launcher JVM`에서 확인한다.

### 서버 실행
macOS / Linux

```bash
./gradlew bootRun
```

Windows (PowerShell)

```powershell
.\gradlew.bat bootRun
```

- 로그에 `Started AssignmentApplication`이 나오면 `http://localhost:8080`에서 요청을 받는다. 서버가 실행되는 동안 Gradle 진행 표시는 `80% EXECUTING`에 머문다. 서버를 끄려면 `Ctrl+C`를 누른다.
- **테이블은 이 명령만으로 만들어진다.** 처음 실행하면 프로젝트 루트에 SQLite 파일 `todo.db`가 생기고, `users`·`todos`·`refresh_tokens` 테이블이 자동으로 생성된다(`ddl-auto: update`). SQL을 직접 실행할 필요는 없다. 데이터를 비우려면 서버를 끄고 `todo.db`를 지운 뒤 다시 실행한다.
- 기동 로그의 경고 `TODO_JWT_SECRET이 설정되지 않아 임의 서명 키를 생성했습니다.`는 정상이다. 서명 키는 설정하지 않아도 된다(아래 참고).

### JWT 서명 키 (선택)
`TODO_JWT_SECRET`을 지정하지 않으면 서버가 기동할 때마다 임의 키를 만든다. 그래서 서버를 재시작하면 전에 받은 `access_token`은 401이 된다. 이때는 다시 로그인하거나 `refresh_token`으로 재발급받는다. 리프레시 토큰은 DB에 저장되므로 재시작한 뒤에도 쓸 수 있다.

재시작해도 `access_token`을 유지하려면 아래처럼 키를 한 번 만들어 두고, 같은 터미널에서 서버를 실행한다. 키는 Base64로 인코딩한 32바이트 이상의 값이어야 한다. 형식이 맞지 않으면 서버가 기동하지 않고 이유를 출력한다.

```bash
export TODO_JWT_SECRET="$(openssl rand -base64 32)"
./gradlew bootRun
```

### 실행이 안 될 때

| 메시지 | 원인과 해결 |
|---|---|
| `zsh: permission denied: ./gradlew`<br>`bash: ./gradlew: Permission denied` | 압축 해제 도구에 따라 `gradlew`의 실행 권한이 빠질 수 있다. `chmod +x gradlew`를 실행한 뒤 다시 실행한다. |
| `Gradle requires JVM 17 or later to run` | `JAVA_HOME`이 JDK 17 미만을 가리킨다. JDK 17 이상으로 바꾼다(macOS 예: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`). |
| `Web server failed to start. Port 8080 was already in use.` | 다른 프로그램이 8080 포트를 쓰고 있다. 다른 포트로 실행한다: `./gradlew bootRun --args='--server.port=8081'`<br>Windows: `.\gradlew.bat bootRun "--args=--server.port=8081"`<br>이때 주소는 `http://localhost:8081`이 된다. |

## 테스트 실행 방법
macOS / Linux

```bash
./gradlew test
```

Windows (PowerShell)

```powershell
.\gradlew.bat test
```

- 환경 변수나 서버 실행이 필요 없다. 테스트는 별도 DB 파일(`build/todo-test.db` 등)을 쓰므로 `todo.db`의 데이터에 영향을 주지 않는다.
- 모두 통과하면 `BUILD SUCCESSFUL`이 출력된다. 테스트별 결과(45건)는 `build/reports/tests/test/index.html`에서 볼 수 있다. 시나리오 테스트 이름에는 `[TEST-01]` 같은 체크리스트 ID가 붙어 있다.
- 코드가 바뀌지 않았으면 Gradle은 테스트를 다시 실행하지 않는다(`UP-TO-DATE`). 다시 실행하려면 `./gradlew test --rerun`을 쓴다.
- 실행 중 출력되는 아래 두 메시지는 결과와 무관하다.
  - `Deprecated Gradle features were used in this build…`: Spring Boot Gradle 플러그인이 Gradle 11에서 제거될 API를 쓰기 때문에 나온다.
  - `OpenJDK 64-Bit Server VM warning: Sharing is only supported…`: Mockito가 테스트 중 에이전트를 붙일 때 나오는 JVM 경고다.

| 명세의 필수 테스트 | 테스트 클래스 (`src/test/java/com/assignment/`) |
|---|---|
| 1. 회원가입 → 로그인 → 발급받은 토큰으로 `/users/me` 조회 성공 | `user/UserAuthFlowTest` |
| 2. TODO 생성 → 목록 조회 → 수정 → 삭제 | `todo/TodoCrudFlowTest` |
| 3. 토큰 없이 요청하면 401 | `global/security/AuthenticationRequiredTest` |
| 4. 존재하지 않는 TODO ID로 요청하면 404 | `todo/TodoNotFoundTest` |

추가 테스트
- 다른 키로 서명했거나 변조했거나 서명이 없거나 만료된 토큰은 401: `global/security/InvalidTokenTest`
- 다른 사용자의 TODO는 목록에 없고, 조회·수정·삭제하면 404: `todo/TodoOwnershipTest`
- 중복 가입 409: `user/DuplicateSignupTest`
- 입력 검증 400: `global/error/ValidationErrorTest`
- 토큰 재발급·회전·재사용 탐지·만료: `user/TokenRefreshFlowTest`, `user/RefreshTokenRotationTest`, `user/RefreshTokenExpiryTest`
- 선택 API: `todo/TodoSearchTest`, `user/UserUpdateTest`, `user/UserWithdrawalTest`
- API 문서(보호 API 표시·성공 상태 코드): `global/config/ApiDocsTest`

모든 테스트는 보안 필터까지 포함한 통합 테스트(`@SpringBootTest` + MockMvc)다. 정상 토큰은 실제 로그인 응답에서 받고, 위조·만료 토큰은 테스트에서 직접 만든다.

## API 목록 요약
기본 주소는 `http://localhost:8080`이고, 요청·응답 본문은 JSON이다. 인증이 "필요"인 API는 `Authorization: Bearer <access_token>` 헤더를 보내야 한다. 명세의 필수 API 8개에 *(확장)* 토큰 재발급과 *(선택)* 가산점 API 3개를 더했다.

**사용자**

| Method | Path | 설명 | 인증 | 성공 | 오류 |
|---|---|---|---|---|---|
| POST | `/users/signup` | 회원가입 | – | `201` `{id, username}` | 400, 409 |
| POST | `/users/login` | 로그인, `access_token` 반환 | – | `200` `{access_token, refresh_token}` | 400, 401 |
| GET | `/users/me` | 내 정보 조회 | 필요 | `200` `{id, username}` | 401, 404 |
| POST | `/users/refresh` | 토큰 재발급 *(확장)* | – | `200` `{access_token, refresh_token}` | 400, 401 |
| PUT | `/users/me` | 회원정보 수정 *(선택)* | 필요 | `200` `{id, username}` | 400, 401, 404, 409 |
| DELETE | `/users/me` | 탈퇴 *(선택)* | 필요 | `204` | 401, 404 |

**TODO** — 로그인한 사용자 본인의 TODO만 다룬다

| Method | Path | 설명 | 인증 | 성공 | 오류 |
|---|---|---|---|---|---|
| POST | `/todos` | 생성 | 필요 | `201` TODO, `Location: /todos/{id}` | 400, 401 |
| GET | `/todos` | 내 목록 조회(id 오름차순) | 필요 | `200` `[TODO]` | 401 |
| GET | `/todos/{id}` | 단건 조회 | 필요 | `200` TODO | 401, 404 |
| PUT | `/todos/{id}` | 수정(전체 교체) | 필요 | `200` TODO | 400, 401, 404 |
| DELETE | `/todos/{id}` | 삭제 | 필요 | `204` | 401, 404 |
| GET | `/todos/search?keyword=` | 제목으로 검색 *(선택)* | 필요 | `200` `[TODO]` | 400, 401 |

TODO는 `{"id":1,"title":"Write README","description":"check every command","completed":false}` 형태다. `description`이 없으면 `null`이다.

### 요청 본문과 검증 규칙

| 요청 | 필드 | 규칙 |
|---|---|---|
| 회원가입 | `username`, `password` | `username` 필수·50자 이하. `password` 8~64자, UTF-8 기준 72바이트 이하 |
| 로그인 | `username`, `password` | 둘 다 필수. 값이 틀리면 401 |
| 토큰 재발급 | `refresh_token` | 필수 |
| TODO 생성 | `title`, `description` | `title` 필수(공백만은 안 됨)·100자 이하. `description`은 선택, 1,000자 이하 |
| TODO 수정 | `title`, `description`, `completed` | 전체 교체라 `title`·`completed` 필수. `description`을 빼면 `null`로 바뀐다 |
| 회원정보 수정 | `username`, `current_password`, `new_password` | `username`·`current_password` 필수. `new_password`는 선택이며 가입 비밀번호와 같은 규칙. 현재 비밀번호가 틀리면 400 |
| 제목 검색 | 쿼리 `keyword` | 필수·100자 이하. 영문 대소문자를 구분하지 않고, `%`·`_`도 글자 그대로 찾는다 |

탈퇴하면 그 사용자의 TODO와 리프레시 토큰이 삭제된다. 탈퇴한 계정은 로그인할 수 없고(401), 같은 username으로 다시 가입할 수도 없다(409).

### 인증
1. `POST /users/login`이 돌려준 `access_token`을 `Authorization: Bearer <access_token>` 헤더로 보낸다. 액세스 토큰은 1시간 동안 유효하다.
2. 액세스 토큰이 만료되면 보호 API가 401을 돌려준다. `POST /users/refresh`에 `{"refresh_token":"<refresh_token>"}`을 보내면 새 토큰 쌍을 받는다. 리프레시 토큰은 7일 동안 유효하고 재발급할 때마다 새것으로 바뀌므로, 항상 가장 최근에 받은 값을 쓴다.
3. 이미 쓴 리프레시 토큰을 다시 보내면 401이다. 이 경우 탈취로 보고 그 사용자의 리프레시 토큰을 모두 폐기하므로 다시 로그인해야 한다. 비밀번호를 바꾸거나 탈퇴해도 그 사용자의 리프레시 토큰은 폐기된다.
4. 액세스 토큰은 DB를 조회하지 않고 서명과 만료 시각만으로 검증하는 JWT다. 그래서 이미 발급된 액세스 토큰은 비밀번호를 바꾸거나 탈퇴한 뒤에도 만료(최대 1시간) 전까지 유효하다.

### 오류 응답
오류는 모두 RFC 9457 ProblemDetail 형식(`application/problem+json`)으로 응답한다. 모든 401 응답에는 `WWW-Authenticate: Bearer` 헤더가 붙는다.

```json
{"type":"about:blank","title":"Not Found","status":404,"detail":"TODO를 찾을 수 없습니다.","instance":"/todos/1"}
```

입력 검증에 실패하면 필드별 오류가 붙는다. 예: `"errors":[{"field":"title","message":"제목은 필수입니다."}]`

| 상태 | 언제 |
|---|---|
| 400 | 입력 검증 실패, 올바르지 않은 JSON, 숫자가 아닌 `{id}`, 회원정보 수정에서 현재 비밀번호 불일치 |
| 401 | 토큰 없음·형식 오류·서명 불일치·만료, 로그인 실패, 유효하지 않은 리프레시 토큰 |
| 404 | 없는 TODO 또는 다른 사용자의 TODO, 토큰의 사용자를 찾을 수 없음(탈퇴 등) |
| 409 | 이미 사용 중인 username(가입·회원정보 수정 — 탈퇴한 계정의 username 포함) |

### Swagger UI
서버를 실행한 뒤 브라우저에서 `http://localhost:8080/swagger-ui.html`을 연다(OpenAPI 문서는 `/v3/api-docs`). 로그인 응답의 `access_token` 값을 **Authorize**에 넣으면(`Bearer `는 붙이지 않는다) 자물쇠가 표시된 보호 API를 화면에서 바로 호출할 수 있다.

## 요청 예시 (curl)
서버를 실행한 터미널과 **다른** macOS/Linux 터미널(bash·zsh)에서 0번부터 차례로 붙여 넣는다. 앞 단계에서 만든 변수(`ACCESS_TOKEN` 등)를 다음 단계가 이어서 쓴다. 각 단계의 → 뒤 숫자가 기대하는 상태 코드다. Windows에서는 위의 Swagger UI로 같은 흐름을 확인할 수 있다.

0\. 서버 주소와 username을 정한다. 포트를 바꿔 실행했다면 `BASE`의 포트를 고친다. username은 실행할 때마다 새로 만든다. 같은 username으로 다시 가입하면 409이기 때문이다.

```bash
BASE=http://localhost:8080
TEST_USER="tester-$(date +%s)"
```

1\. 회원가입 → `201`

```bash
curl -i -w '\n' -X POST "$BASE/users/signup" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$TEST_USER\",\"password\":\"password1234\"}"
```

2\. 로그인 → `200`. 응답의 토큰을 변수에 담는다.

```bash
RESPONSE=$(curl -si -X POST "$BASE/users/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$TEST_USER\",\"password\":\"password1234\"}")
echo "$RESPONSE"
ACCESS_TOKEN=$(echo "$RESPONSE" | sed -nE 's/.*"access_token":"([^"]+)".*/\1/p')
REFRESH_TOKEN=$(echo "$RESPONSE" | sed -nE 's/.*"refresh_token":"([^"]+)".*/\1/p')
```

3\. 내 정보 조회 → `200`, 토큰 없이 TODO 목록 요청 → `401`

```bash
curl -i -w '\n' "$BASE/users/me" -H "Authorization: Bearer $ACCESS_TOKEN"
curl -i -w '\n' "$BASE/todos"
```

4\. TODO 생성 → `201`. 응답의 `id`를 변수에 담는다.

```bash
RESPONSE=$(curl -si -X POST "$BASE/todos" -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' -d '{"title":"Write README","description":"check every command"}')
echo "$RESPONSE"
TODO_ID=$(echo "$RESPONSE" | sed -nE 's/.*"id":([0-9]+).*/\1/p')
```

5\. 내 목록 조회 → `200`, 단건 조회 → `200`

```bash
curl -i -w '\n' "$BASE/todos" -H "Authorization: Bearer $ACCESS_TOKEN"
curl -i -w '\n' "$BASE/todos/$TODO_ID" -H "Authorization: Bearer $ACCESS_TOKEN"
```

6\. 수정(전체 교체) → `200`

```bash
curl -i -w '\n' -X PUT "$BASE/todos/$TODO_ID" -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' -d '{"title":"Write README","description":"done","completed":true}'
```

7\. 삭제 → `204`, 삭제한 TODO 조회 → `404`

```bash
curl -i -w '\n' -X DELETE "$BASE/todos/$TODO_ID" -H "Authorization: Bearer $ACCESS_TOKEN"
curl -i -w '\n' "$BASE/todos/$TODO_ID" -H "Authorization: Bearer $ACCESS_TOKEN"
```

8\. *(확장)* 토큰 재발급 → `200`. 새 토큰 쌍으로 변수를 바꾼다. 한 번 쓴 리프레시 토큰은 다시 쓸 수 없다.

```bash
RESPONSE=$(curl -si -X POST "$BASE/users/refresh" -H 'Content-Type: application/json' \
  -d "{\"refresh_token\":\"$REFRESH_TOKEN\"}")
echo "$RESPONSE"
ACCESS_TOKEN=$(echo "$RESPONSE" | sed -nE 's/.*"access_token":"([^"]+)".*/\1/p')
REFRESH_TOKEN=$(echo "$RESPONSE" | sed -nE 's/.*"refresh_token":"([^"]+)".*/\1/p')
```

9\. *(선택)* 검색할 TODO 생성 → `201`, 제목 검색 → `200`. 검색은 영문 대소문자를 구분하지 않는다.

```bash
curl -i -w '\n' -X POST "$BASE/todos" -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' -d '{"title":"Buy milk"}'
curl -i -w '\n' "$BASE/todos/search?keyword=MILK" -H "Authorization: Bearer $ACCESS_TOKEN"
```

10\. *(선택)* 회원정보 수정(비밀번호 변경) → `200`

```bash
curl -i -w '\n' -X PUT "$BASE/users/me" -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$TEST_USER\",\"current_password\":\"password1234\",\"new_password\":\"password5678\"}"
```

11\. *(선택)* 탈퇴 → `204`, 탈퇴한 계정으로 로그인 → `401`

```bash
curl -i -w '\n' -X DELETE "$BASE/users/me" -H "Authorization: Bearer $ACCESS_TOKEN"
curl -i -w '\n' -X POST "$BASE/users/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$TEST_USER\",\"password\":\"password5678\"}"
```

## 프로젝트 구조
기능(user·todo)마다 헥사고날 구조(도메인 ← 애플리케이션 ← 어댑터)로 나눴다. 두 기능은 서로 직접 참조하지 않는다. 탈퇴 때 TODO를 지우는 일은 이벤트로 넘긴다.

```text
src/main/java/com/assignment
├── user/            회원가입·로그인·토큰 재발급·내 정보(수정·탈퇴)
│   ├── domain/      User, RefreshToken — 프레임워크에 의존하지 않는 도메인 모델
│   ├── application/ 유스케이스(port/in)·외부 포트(port/out)·서비스·Command/Result
│   └── adapter/     in/web(컨트롤러·요청/응답 DTO), out/persistence(JPA), out/security(비밀번호 해시·JWT 발급), out/event
├── todo/            TODO CRUD·제목 검색 — user와 같은 구조, in/event(탈퇴한 사용자의 TODO 삭제)
└── global/          security(JWT 검증·401 응답), error(ProblemDetail 변환), config(API 문서), event(기능 간 이벤트)
```

## 문서

| 경로 | 내용 |
|---|---|
| `prompts/used_prompts.md` | AI 활용 기록 — 사용한 주요 프롬프트와 결과를 확인·수정한 방법 |
| `docs/requirements-checklist.md` | 명세 요구사항별 구현 위치와 검증 근거 |
| `docs/decisions.md` | 설계 결정과 근거 |
| `docs/progress.md` | 단계별 작업·검증 기록 |
| `CLAUDE.md`, `.claude/` | AI 작업 지침(규칙·리뷰 에이전트·제출 점검 절차) |
