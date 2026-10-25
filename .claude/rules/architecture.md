---
paths:
  - "src/main/java/**/*.java"
---

# 아키텍처 규칙 — 헥사고날 + 경량 DDD (근거 D-03·D-04)

## 패키지 구조 (기본 패키지 `com.assignment`)
```text
com.assignment
├── user | todo                          # 기능 단위
│   ├── domain                           # 엔티티·값 객체·도메인 규칙 (순수 Java)
│   ├── application
│   │   ├── port/in                      # 책임별 유스케이스 인터페이스 (UserUseCase, AuthUseCase, TodoUseCase)
│   │   ├── port/out                     # 영속성·외부 기능 포트
│   │   └── service                      # 유스케이스 구현 (UserService, AuthService, TodoService)
│   └── adapter
│       ├── in/web                       # Controller(UserController, AuthController, TodoController), Request/Response DTO (record)
│       └── out/persistence, out/security   # JPA 어댑터 / (user) 토큰 발급·비밀번호 해시 어댑터
└── global
    ├── security                         # SecurityFilterChain, 서명 키·JwtEncoder/Decoder 빈, 공개 인증 경로용 BearerTokenResolver, EntryPoint, @CurrentUserId
    ├── error                            # GlobalExceptionHandler (예외 → ProblemDetail)
    └── config                           # springdoc OpenAPI 설정
```

## 의존 방향 (위반 금지)
- `domain`: 다른 계층·프레임워크에 의존하지 않는다. `org.springframework`, `jakarta.persistence`, `com.fasterxml` import 금지 (Lombok `@Getter`만 허용).
- `application`: `domain`만 의존한다. 프레임워크는 `@Service`, `@Transactional`과 설정값을 받는 생성자 파라미터의 `@Value`(예: 리프레시 토큰 수명)만 허용하고 웹·JPA·Spring Data·Spring Security 타입은 쓰지 않는다.
- `adapter.in.web`: `application.port.in`만 호출한다(응답 변환을 위해 `domain` 읽기는 가능). 서비스 구현체·리포지토리를 직접 호출하지 않는다.
- `adapter.out.*`: `application.port.out`을 구현하고 `domain`을 사용한다. `global`은 공통 설정만 담고 기능 로직을 두지 않는다.
- 어댑터 패키지(`adapter.*`)의 클래스(컨트롤러·DTO·JPA 엔티티·Spring Data 리포지토리·어댑터 구현)는 package-private으로 둔다 — 포트 밖에서 참조하면 컴파일 오류가 난다. 도메인·포트·서비스·기능 예외는 public이다.
- 기능 간(`user` ↔ `todo`) 직접 참조 금지. TODO는 소유자를 `userId`(Long)로만 참조한다(집합체 간 ID 참조).

## 포트 정책
- In Port: 책임별 인터페이스 — `UserUseCase`(가입·내 정보), `AuthUseCase`(로그인·토큰 재발급), `TodoUseCase`(생성·목록·단건·수정·삭제). 호출자가 컨트롤러 하나뿐이라 유스케이스마다 쪼개지 않는다.
- 파라미터가 많은 유스케이스(예: TODO 수정)만 Command record를 쓰고 나머지는 값을 직접 받는다. 반환은 도메인 객체(토큰 발급은 토큰 쌍 record)이고, 응답 변환은 웹 어댑터가 한다.
- Out Port: `UserRepository`, `TodoRepository`, `RefreshTokenRepository`, `PasswordHasher`, `TokenIssuer`. 이 외의 포트·인터페이스가 필요하면 먼저 `docs/decisions.md`에 이유를 남긴다.
- 리프레시 토큰 생성(`SecureRandom`)·해시(SHA-256 `MessageDigest`)는 JDK 표준 API로 처리하고 별도 포트를 만들지 않는다.
- Mapper 인터페이스·Facade·단순 위임 클래스를 만들지 않는다. 변환은 DTO·엔티티의 정적 메서드(`from`, `toDomain`)로 한다.

## 도메인·DTO
- 도메인 객체가 불변식을 지킨다(생성 팩터리·행위 메서드에서 검증). 상태 변경은 의미 있는 메서드(`todo.update(...)`)로만 하고 setter는 두지 않는다.
- 입력 형식 검증은 Request DTO(Bean Validation)가 먼저 막는다. 도메인 검증은 안전망이다.
- 도메인 객체·JPA 엔티티를 API 응답으로 직접 반환하지 않는다. JPA 엔티티는 `adapter.out.persistence` 밖으로 나가지 않는다.

## 코드 규칙
- 생성자 주입만 사용한다(필드 주입 금지).
- Lombok은 `@Getter`, `@RequiredArgsConstructor`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 위주로 쓰고 `@Data`·`@Setter`는 금지한다. DTO·Command는 record로 만든다.
- 기능 예외(예: `TodoNotFoundException`)는 해당 기능의 `application` 패키지에 두고 HTTP 개념(`@ResponseStatus`, `HttpStatus`)을 넣지 않는다. 예외 → HTTP 상태 매핑은 `global.error.GlobalExceptionHandler`에서만 하며, 이를 위해 `global.error`가 기능 예외를 참조하는 것만 허용한다.
- 클래스명: `<대상>UseCase`, `<대상>Service`, `<대상>Controller`, `<대상>PersistenceAdapter`, `<대상>JpaEntity`, `<대상>JpaRepository`, `<동작><대상>Request`, `<대상>Response`.

## 점검 명령
```bash
grep -rnE 'import (org\.springframework|jakarta\.persistence|com\.fasterxml)' src/main/java/com/assignment/*/domain  # 출력이 없어야 한다
```
