---
paths:
  - "src/main/java/**/adapter/in/web/**"
  - "src/main/java/**/global/**"
  - "src/test/**"
  - "README.md"
---

# API 계약 (근거 D-02·D-07·D-08·D-12·D-15)

## 공통
- 로그인·재발급 응답은 `{"access_token": "<JWT>", "refresh_token": "<불투명 토큰>"}`이다. `access_token`은 명세 필드이고 `refresh_token`은 사용자 요청 확장(D-15)이다. 두 필드 모두 응답 record에 `@JsonProperty`로 snake_case를 명시한다. 나머지 필드는 모두 한 단어이며, 여러 단어 필드를 추가하면 같은 방식으로 명시한다(전역 네이밍 전략은 springdoc 스키마에 반영되지 않아 쓰지 않는다).
- 인증 헤더는 `Authorization: Bearer <access_token>`. 액세스 토큰이 만료되면 클라이언트는 `POST /users/refresh`로 새 토큰 쌍을 받고 원래 요청을 다시 보낸다.
- 오류 본문은 RFC 9457 ProblemDetail(`application/problem+json`): `type`, `title`, `status`, `detail`, `instance`(검증 실패 시 `errors` 추가). 401도 같은 형식에 `WWW-Authenticate: Bearer` 헤더를 유지한다.
- 상태 코드: 검증 실패 400 · 인증 실패 401 · 리소스 없음/타인 소유 404 · username 중복 409.
- API 문서: `/swagger-ui.html`(springdoc). `@SecurityRequirement`는 보호 API에만 붙인다(가입·로그인·재발급 제외).

## 엔드포인트
| ID | Method · Path | 인증 | 요청 본문 | 성공 | 응답 본문 | 오류 |
|---|---|---|---|---|---|---|
| API-01 | `POST /users/signup` | ✗ | `username`, `password` | 201 | `{id, username}` | 400, 409 |
| API-02 | `POST /users/login` | ✗ | `username`, `password` | 200 | `{access_token, refresh_token}` | 400, 401 |
| API-03 | `GET /users/me` | ✓ | – | 200 | `{id, username}` | 401 |
| API-11 | `POST /users/refresh` | ✗ | `refresh_token` | 200 | `{access_token, refresh_token}` | 400, 401 |
| API-04 | `POST /todos` | ✓ | `title`, `description`(선택) | 201 + `Location: /todos/{id}` | Todo | 400, 401 |
| API-05 | `GET /todos` | ✓ | – | 200 | `[Todo]` (내 것만, id 오름차순) | 401 |
| API-06 | `GET /todos/{id}` | ✓ | – | 200 | Todo | 401, 404 |
| API-07 | `PUT /todos/{id}` | ✓ | `title`, `description`(선택), `completed` — 전체 교체 | 200 | Todo | 400, 401, 404 |
| API-08 | `DELETE /todos/{id}` | ✓ | – | 204 | – | 401, 404 |

- Todo = `{id, title, description, completed}`. 생성 시 `completed`는 `false`. 비밀번호(해시)·소유자 정보·토큰 해시는 어떤 응답에도 넣지 않는다.
- 입력 검증: `username` 필수·50자 이하 / `password` 8~64자·UTF-8 72바이트 이하 / `refresh_token` 필수 / `title` 필수·공백 불가·100자 이하 / `description` 1,000자 이하 / `completed`는 PUT에서 필수.
- 경로의 `{id}`가 숫자가 아니면 400.

## 구현 규칙
- 컨트롤러는 요청 DTO `@Valid` 검증 → In Port 호출 → Response DTO 변환만 한다. 비즈니스 로직·리포지토리 접근 금지.
- 예외 → ProblemDetail 변환은 `GlobalExceptionHandler`(`ResponseEntityExceptionHandler` 상속) 한 곳에서 한다.
- README의 API 목록은 이 표와 일치시킨다.
