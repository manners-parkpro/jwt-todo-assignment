---
paths:
  - "src/main/java/**/*.java"
---

# 아키텍처 규칙 — 헥사고날 + 경량 DDD (근거 D-03·D-04·D-20)

## 패키지 구조 (기본 패키지 `com.assignment`)
```text
com.assignment
├── user | todo                          # 기능 단위
│   ├── domain                           # 엔티티·값 객체·도메인 규칙 (순수 Java)
│   ├── application
│   │   ├── dto                          # Command(쓰기 입력)·Result(유스케이스 결과) record
│   │   ├── port/in                      # 책임별 유스케이스 인터페이스 (UserUseCase, AuthUseCase, TodoUseCase)
│   │   ├── port/out                     # <대상>CommandPort·<대상>QueryPort(영속화), <대상>Port(그 외)
│   │   └── service                      # 유스케이스 구현 (UserService, AuthService, TodoService)
│   └── adapter
│       ├── in/web                       # Controller, Request/Response record
│       └── out/persistence, out/security   # <대상>RepositoryAdapter·<대상>Repository(Spring Data)·<대상>JpaEntity / (user) <대상>Adapter(토큰 발급·비밀번호 해시)
└── global
    ├── security                         # SecurityFilterChain, 서명 키·JwtEncoder/Decoder 빈, 공개 인증 경로용 BearerTokenResolver, EntryPoint, @CurrentUserId
    ├── error                            # GlobalExceptionHandler (예외 → ProblemDetail)
    └── config                           # springdoc OpenAPI 설정
```

## 의존 방향 (위반 금지)
- `domain`: 다른 계층·프레임워크에 의존하지 않는다. `org.springframework`, `jakarta.persistence`, `com.fasterxml` import 금지 (Lombok `@Getter`만 허용).
- `application`: `domain`만 의존한다. 프레임워크는 `@Service`, `@Transactional`과 설정값을 받는 생성자 파라미터의 `@Value`(예: 리프레시 토큰 수명)만 허용하고 웹·JPA·Spring Data·Spring Security 타입은 쓰지 않는다.
- `adapter.in.web`: `application.port.in`(호출)과 `application.dto`(Command 생성·Result 변환), `global`의 공통 어노테이션·상수(`@CurrentUserId`, `OpenApiConfig.BEARER_AUTH`)만 의존한다. `domain`을 참조하지 않고, 서비스 구현체·리포지토리를 직접 호출하지 않는다 — 웹 응답이 도메인 모델에 묶이지 않게 한다(D-20).
- `adapter.out.*`: `application.port.out`을 구현하고 `domain`을 사용한다. `global`은 공통 설정만 담고 기능 로직을 두지 않는다.
- 어댑터 패키지(`adapter.*`)의 클래스(컨트롤러·DTO·JPA 엔티티·Spring Data 리포지토리·어댑터 구현)는 package-private으로 둔다 — 포트 밖에서 참조하면 컴파일 오류가 난다. 도메인·포트·서비스·기능 예외는 public이다.
- 기능 간(`user` ↔ `todo`) 직접 참조 금지. TODO는 소유자를 `userId`(Long)로만 참조한다(집합체 간 ID 참조).

## 포트 정책
- In Port: 책임별 인터페이스 — `UserUseCase`(가입·내 정보), `AuthUseCase`(로그인·토큰 재발급), `TodoUseCase`(생성·목록·단건·수정·삭제). 호출자가 컨트롤러 하나뿐이라 유스케이스마다 쪼개지 않는다.
- 입력: 요청 본문이 있는 쓰기 유스케이스는 Request의 `toCommand(...)`로 만든 Command를 받는다. 경로·인증에서 오는 값(사용자 ID·TODO ID)만 쓰는 유스케이스는 값을 직접 받는다.
- 반환: 도메인 객체가 아니라 Result record다(도메인 → Result 변환은 application 안에서 `Result.from(domain)`). 웹 어댑터는 `Response.from(result)`로 바꾼다.
- Out Port: 영속화는 `<대상>CommandPort`(쓰기)·`<대상>QueryPort`(읽기) — `UserCommandPort`·`UserQueryPort`, `TodoCommandPort`·`TodoQueryPort`, `RefreshTokenCommandPort`·`RefreshTokenQueryPort`(8단계). 그 밖에 값을 돌려받는 포트는 `<대상>Port` — `PasswordHashPort`, `AccessTokenPort`. 이 외의 포트·인터페이스가 필요하면 먼저 `docs/decisions.md`에 이유를 남긴다.
- 리프레시 토큰 생성(`SecureRandom`)·해시(SHA-256 `MessageDigest`)는 JDK 표준 API로 처리하고 별도 포트를 만들지 않는다.
- Mapper 인터페이스·Facade·단순 위임 클래스를 만들지 않는다. 변환은 DTO·엔티티의 메서드(`toCommand`, `from`, `toDomain`)로 한다.

## 도메인·DTO
- 도메인 객체가 불변식을 지킨다(생성 팩터리·행위 메서드에서 검증). 상태 변경은 의미 있는 메서드(`todo.update(...)`)로만 하고 setter는 두지 않는다.
- 입력 형식 검증은 Request DTO(Bean Validation)가 먼저 막는다. 도메인 검증은 안전망이다.
- 도메인 객체는 application 밖(웹 어댑터)으로 나가지 않고, JPA 엔티티는 `adapter.out.persistence` 밖으로 나가지 않는다.

## 코드 규칙
- 생성자 주입만 사용한다(필드 주입 금지).
- Lombok은 `@Getter`, `@RequiredArgsConstructor`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 위주로 쓰고 `@Data`·`@Setter`는 금지한다. DTO·Command는 record로 만든다.
- 기능 예외(예: `TodoNotFoundException`)는 해당 기능의 `application` 패키지에 두고 HTTP 개념(`@ResponseStatus`, `HttpStatus`)을 넣지 않는다. 예외 → HTTP 상태 매핑은 `global.error.GlobalExceptionHandler`에서만 하며, 이를 위해 `global.error`가 기능 예외를 참조하는 것만 허용한다.
- 여러 줄에 걸친 생성자·record 인스턴스화는 인자를 한 줄에 하나씩 쓴다.

## 네이밍 (사용자가 지정한 참고 프로젝트의 규칙 — D-20)
| 종류 | 이름 | 위치 | 예 |
|---|---|---|---|
| 웹 요청·응답 | `<동작><대상>Request`·`<대상>Response` | `adapter/in/web` | `CreateTodoRequest`, `TodoResponse` |
| 유스케이스 입력·결과 | `<동작><대상>Command`·`<대상>Result` | `application/dto` | `UpdateTodoCommand`, `TodoResult` |
| In Port·구현 | `<대상>UseCase`·`<대상>Service` | `application/port/in`·`service` | `TodoUseCase`, `TodoService` |
| Out Port | `<대상>CommandPort`·`<대상>QueryPort`(영속화), `<대상>Port`(그 외) | `application/port/out` | `TodoQueryPort`, `AccessTokenPort` |
| 영속화 어댑터 | `<대상>RepositoryAdapter`(Spring Data `<대상>Repository`를 주입), 엔티티 `<대상>JpaEntity` | `adapter/out/persistence` | `TodoRepositoryAdapter` |
| 그 밖의 어댑터 | `<대상>Adapter`(리포지토리를 주입하지 않음) | `adapter/out/security` | `PasswordHashAdapter` |

- 참고 프로젝트와 다른 점: Request/Response는 `adapter/in/dto`로 나누지 않고 컨트롤러와 같은 `adapter/in/web`에 둔다 — 나누면 public이어야 해서 package-private 경계(위 의존 방향)가 깨진다. JPA 엔티티는 도메인 모델과 분리했으므로(D-04) `<대상>JpaEntity`로 구분한다.

## 점검 명령
```bash
grep -rnE 'import (org\.springframework|jakarta\.persistence|com\.fasterxml)' src/main/java/com/assignment/*/domain  # 출력이 없어야 한다
```
