---
paths:
  - "src/main/java/**/adapter/in/web/**"
  - "src/main/java/**/global/**"
  - "src/test/**"
  - "README.md"
---

# API 계약 (근거 D-02·D-07·D-08·D-12·D-15)

## 공통
- 로그인·재발급 응답은 `{"access_token": "<JWT>", "refresh_token": "<불투명 토큰>"}`이다. `access_token`은 명세 필드이고 `refresh_token`은 사용자 요청 확장(8단계, D-15)이다. 여러 단어 필드(`access_token`, `refresh_token`)는 요청 record(재발급 요청 본문)와 응답 record 모두 `@JsonProperty`로 snake_case를 명시한다 — 빠뜨리면 요청 값이 null로 바인딩된다. 나머지 필드는 모두 한 단어이며, 여러 단어 필드를 추가하면 같은 방식으로 명시한다(전역 네이밍 전략은 springdoc 스키마에 반영되지 않아 쓰지 않는다).
- 인증 헤더는 `Authorization: Bearer <access_token>`. 액세스 토큰이 만료되면 클라이언트는 `POST /users/refresh`로 새 토큰 쌍을 받고 원래 요청을 다시 보낸다.
- 오류 본문은 RFC 9457 ProblemDetail(`application/problem+json`): `type`, `title`, `status`, `detail`, `instance`(검증 실패 시 `errors` 추가). 모든 401(토큰 없음·무효·만료, 로그인·재발급 실패)은 같은 형식에 `WWW-Authenticate: Bearer` 헤더를 넣는다(RFC 9110 §15.5.2 MUST).
- 상태 코드: 검증 실패 400 · 인증 실패 401 · 리소스 없음/타인 소유 404 · username 중복 409.
- 직접 작성하는 `detail`(인증 실패·기능 예외·검증 메시지)은 한국어로 쓴다. Spring MVC 표준 예외의 기본 `detail`(읽을 수 없는 본문·타입 불일치·없는 경로 등)은 영문 그대로 둔다(D-07). 어떤 `detail`에도 내부 정보(스택 트레이스·SQL·토큰·키)를 넣지 않는다.
- API 문서: `/swagger-ui.html`(springdoc). 문서 정보·Bearer 보안 스키마(`bearerAuth`)·태그 순서(사용자 → TODO)는 `global.config.OpenApiConfig` 한 곳에 둔다. `@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)`는 보호 API에만 붙인다(가입·로그인·재발급 제외). 엔드포인트마다 `@Operation(summary)`를 명세의 설명 문구로 단다.
  - `ResponseEntity`로 상태를 정하는 API(`POST /todos` 201)는 springdoc이 200으로 표시하므로 `@ApiResponse(responseCode)`로 실제 코드를 적는다. 오류 응답은 엔드포인트마다 적지 않고 문서 설명(`@Info`)과 README에 모아 둔다(컨트롤러 가독성).
  - `ApiDocsTest`(API-10)가 문서의 모든 API를 기대 목록과 비교해 보호 API에만 `[{bearerAuth: []}]`가 붙었는지, 실제 성공 코드·토큰 필드 이름을 검증한다 — API를 추가하면 이 테스트의 목록도 함께 고친다.

## 엔드포인트
| ID | Method · Path | 인증 | 요청 본문 | 성공 | 응답 본문 | 오류 |
|---|---|---|---|---|---|---|
| API-01 | `POST /users/signup` | ✗ | `username`, `password` | 201 | `{id, username}` | 400, 409 |
| API-02 | `POST /users/login` | ✗ | `username`, `password` | 200 | `{access_token, refresh_token}` — `access_token`은 명세 필드, `refresh_token`은 확장(8단계) | 400, 401 |
| API-03 | `GET /users/me` | ✓ | – | 200 | `{id, username}` | 401, 404(사용자 없음) |
| API-11 | `POST /users/refresh` | ✗ | `refresh_token` | 200 | `{access_token, refresh_token}` | 400, 401 |
| API-04 | `POST /todos` | ✓ | `title`, `description`(선택) | 201 + `Location: /todos/{id}` | Todo | 400, 401 |
| API-05 | `GET /todos` | ✓ | – | 200 | `[Todo]` (내 것만, id 오름차순) | 401 |
| API-06 | `GET /todos/{id}` | ✓ | – | 200 | Todo | 401, 404 |
| API-07 | `PUT /todos/{id}` | ✓ | `title`, `description`(선택), `completed` — 전체 교체 | 200 | Todo | 400, 401, 404 |
| API-08 | `DELETE /todos/{id}` | ✓ | – | 204 | – | 401, 404 |
| OPT-01 | `GET /todos/search?keyword=` (선택) | ✓ | – (쿼리 `keyword` 필수·100자 이하) | 200 | `[Todo]` — 내 것 중 제목에 검색어 포함, id 오름차순 | 400, 401 |
| OPT-02 | `PUT /users/me` (선택) | ✓ | `username`, `current_password`, `new_password`(선택) | 200 | `{id, username}` | 400(검증·현재 비밀번호 불일치), 401, 404, 409 |
| OPT-03 | `DELETE /users/me` (선택) | ✓ | – | 204 | – | 401, 404 |

- 선택 API(OPT-01~03, D-21 — S15 구현). 검색은 ASCII 대소문자를 구분하지 않고 `%`·`_`를 문자 그대로 찾는다. 수정은 현재 비밀번호를 먼저 확인한다(틀리면 400 — 401은 토큰 문제에만 쓴다). 탈퇴한 계정의 로그인·재발급은 401, 남은 액세스 토큰의 `/users/me`는 404, 같은 username 재가입은 409다.

- Todo = `{id, title, description, completed}`. 생성 시 `completed`는 `false`. 비밀번호(해시)·소유자 정보·토큰 해시는 어떤 응답에도 넣지 않는다.
- 입력 검증: 가입 `username` 필수·50자 이하, `password` 필수·8~64자·UTF-8 72바이트 이하 / 로그인 `username`·`password` 필수(가입 규칙은 검사하지 않는다 — 틀리면 401) / `refresh_token` 필수 / `title` 필수·100자 이하, `description` 1,000자 이하(생성·수정 같음) / `completed`는 PUT에서 필수 / 회원정보 수정 `username` 필수·50자 이하, `current_password` 필수, `new_password`는 보냈을 때만 가입 비밀번호 규칙. 문자열의 "필수"는 `@NotBlank`라 공백만 있어도 400이다 — README 검증 규칙 표도 같은 기준으로 적는다(S18 실측).
- 검증 메시지는 어노테이션에 고정한 한국어로 쓴다 — 기본 메시지는 요청 `Accept-Language`·JVM 로캘에 따라 바뀐다. 실패 응답은 `errors: [{field, message}]`이고, `field`는 클라이언트가 보낸 JSON 이름이다(`refresh_token` — 자바 이름 `refreshToken`을 `GlobalExceptionHandler`가 Jackson 정보로 바꾼다).
- 재발급 실패(없는 토큰·만료·이미 쓴 토큰)는 모두 같은 401과 detail("리프레시 토큰이 유효하지 않습니다.")이다. 토큰 응답에는 Spring Security 기본 헤더(`Cache-Control: no-cache, no-store, max-age=0, must-revalidate`)가 붙는다(S12 실측).
- `GET /users/me`에서 토큰은 유효하지만 사용자가 없으면 404다.
- 경로의 `{id}`가 숫자가 아니면 400.

## 구현 규칙
- 구현 순서: 필수 흐름(로그인 응답 = `access_token`)을 먼저 완성하고, `refresh_token` 필드와 `POST /users/refresh`는 재발급 확장 단계에서 추가한다. 필수 테스트가 확장에 의존하지 않게 하기 위해서다(D-15).
- 컨트롤러는 요청 DTO `@Valid` 검증 → `toCommand(...)` → In Port 호출 → 돌려받은 Result를 `Response.from(result)`로 변환만 한다(도메인 참조 금지 — D-20). 비즈니스 로직·리포지토리 접근 금지.
- 예외 → ProblemDetail 변환은 `GlobalExceptionHandler`(`ResponseEntityExceptionHandler` 상속) 한 곳에서 한다.
- README의 API 목록은 이 표와 일치시킨다.
