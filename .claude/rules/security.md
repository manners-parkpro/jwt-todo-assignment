---
paths:
  - "src/main/**"
  - "src/test/**"
---

# 보안 규칙 (근거 D-01·D-05·D-06·D-08·D-09·D-15)

## 공통 보안 구성 — 인증 코드는 이곳 한 군데에만 둔다
- `SecurityFilterChain`: CSRF 비활성(쿠키 없는 Bearer API), 세션 `STATELESS`, formLogin·httpBasic·logout 비활성(기본 로그아웃 필터는 `/logout`을 인증 없이 302로 처리한다 — 5단계 리뷰에서 확인).
- `permitAll`: `POST /users/signup`, `POST /users/login`, `POST /users/refresh`, `DispatcherType.ERROR`, Swagger(`/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`). 나머지는 `anyRequest().authenticated()`.
  - ERROR 디스패치를 허용하지 않으면 400·500 응답이 401로 바뀐다(AuthorizationFilter가 모든 디스패치 유형에 적용된다).
- 공개 인증 엔드포인트(가입·로그인·재발급)에서는 `Authorization` 헤더를 무시하는 `BearerTokenResolver`를 쓴다. 리소스 서버는 permitAll 경로에서도 헤더의 토큰을 검증하므로, 그대로 두면 만료 토큰이 실린 재발급 요청이 401로 막힌다.
- `oauth2ResourceServer(...)`: 토큰 없음·무효·만료 → 401. EntryPoint는 `BearerTokenAuthenticationEntryPoint`에 먼저 위임해 상태 코드와 `WWW-Authenticate`를 유지한 뒤 ProblemDetail 본문을 쓴다.
- 직접 만드는 요청 매처는 `PathPatternRequestMatcher`(6.5+)를 쓴다 — `AntPathRequestMatcher`는 제거 예정(`@Deprecated(forRemoval = true)`)이다. 문자열 `requestMatchers(...)`는 프레임워크가 매처를 고르므로 그대로 쓴다. 공개 엔드포인트 목록은 `SecurityConfig`의 매처 하나로 permitAll과 헤더 무시에 함께 쓴다(재발급 경로는 8단계에 추가).

## 액세스 토큰 (JWT, HS256)
- 설정: `auth.jwt-secret: ${JWT_SECRET:}`, `auth.access-token-ttl: 1h`.
- 키: `JWT_SECRET`(Base64)이 없으면 기동 시 `SecureRandom` 32바이트 키를 만들고 WARN 로그를 남긴다(재시작하면 기존 액세스 토큰은 무효 → 리프레시 토큰으로 재발급). 값이 Base64가 아니거나 디코딩 결과가 32바이트 미만이면 기동을 실패시킨다. **고정 키·기본 키를 코드·설정 파일·테스트 리소스에 두지 않는다.**
- 발급: `new NimbusJwtEncoder(new ImmutableSecret<>(secretKey))`. 파라미터에 **`JwsHeader.with(MacAlgorithm.HS256)`를 명시**한다 — 기본 헤더가 RS256이라 생략하면 서명 키를 찾지 못해 실패한다.
- 클레임: `sub`=사용자 ID(문자열), `iat`, `exp`. 사용자명·비밀번호 해시 같은 개인정보와 쓰지 않는 클레임(`iss` 등)은 넣지 않는다.
- 검증: `NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()` 후 `setJwtValidator(new JwtTimestampValidator(Duration.ZERO))` → 서명·`exp`/`nbf`. 발급과 검증이 같은 서버라 clock skew를 두지 않는다(기본값 60초면 만료 후에도 통과한다).
- 현재 사용자 ID는 JWT `sub`에서만 얻는다. 공통 메타 어노테이션(예: `@CurrentUserId` ← `@AuthenticationPrincipal(expression = ...)`) 하나로 추출하고 컨트롤러마다 파싱하지 않는다. 요청 바디·파라미터로 받은 사용자 ID는 신뢰하지 않는다.

## 리프레시 토큰 (불투명 토큰, D-15)
- 형식: `SecureRandom` 32바이트 → Base64URL(패딩 없음). JWT가 아니라 서명 키와 무관하므로 서버를 재시작해도 유효하다.
- 저장: SHA-256 해시만 `refresh_tokens`에 저장한다. 원문은 응답으로 한 번만 내보내고 저장·로그하지 않는다. 만료는 `auth.refresh-token-ttl`(7d).
- 회전: 재발급할 때마다 사용한 토큰을 `used`로 표시하고 새 액세스 토큰과 새 리프레시 토큰을 함께 발급한다.
- 재사용 탐지: 이미 사용한 토큰이 다시 오면 그 사용자의 리프레시 토큰을 모두 폐기하고 401로 응답한다(RFC 9700 §4.14.2).
  - 폐기한 뒤 401 예외를 던지면 기본 롤백 규칙(RuntimeException)에 따라 폐기가 취소된다 → 재발급 메서드는 `@Transactional(noRollbackFor = <재발급 실패 예외>)`로 폐기를 커밋하고, 예외는 그 메서드 안에서 직접 던진다(같은 트랜잭션의 다른 `@Transactional` 경계를 거쳐 나오면 rollback-only가 돼 커밋 때 500). `REQUIRES_NEW`로 분리하지 않는다 — 커넥션 풀이 1개라 30초(connectionTimeout) 대기 뒤 실패하고, 풀을 늘려도 SQLite 잠금으로 실패한다(D-10). TEST-08의 "재사용 뒤 최신 토큰도 401"이 이 동작을 검증한다.
- 없는 토큰·만료·재사용은 구분하지 않고 같은 401·같은 메시지로 응답한다. 로그인할 때 그 사용자의 만료된 토큰 행을 정리한다.

## 비밀번호·로그인
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()`(기본 bcrypt)는 `PasswordHashAdapter`(`PasswordHashPort` 구현) 안에서만 만들어 쓰고 빈으로 등록하지 않는다. 평문 저장 금지.
- BCrypt는 인코딩할 때 72바이트를 넘는 비밀번호에 `IllegalArgumentException`을 던진다(비교는 예외 없이 앞 72바이트로 한다 — Spring Security 6.5.11 `BCrypt.hashpw`의 `for_check`) → 가입 요청 검증으로 8~64자 + UTF-8 72바이트 이하를 강제한다(미검증 시 500).
- 로그인 실패는 "없는 사용자"와 "비밀번호 불일치"를 구분하지 않고 같은 401·같은 메시지로 응답한다.

## 소유권
- TODO 단건 조회·수정·삭제는 `id`와 `userId`를 함께 조건으로 조회한다. 미존재와 타인 소유는 똑같이 404로 응답한다(존재 은닉).
- 목록은 `userId` 조건으로만 조회한다.

## 사용 금지 기능 (Boot 3.5 OSS 종료 후 공개된 CVE의 전제 조건 — D-01)
- 임베디드 LDAP(`UnboundIdContainer`, `spring.ldap.embedded.*`), `AesBytesEncryptor`, 요청값으로 만든 SpEL 평가, 네이티브 쿼리 + 요청값 `Sort`. (`@AuthenticationPrincipal`의 상수 SpEL은 허용)

## 비밀·로그
- 비밀번호·액세스 토큰·리프레시 토큰·키·`Authorization` 헤더 값을 로그·예외 메시지·응답(발급 응답 제외)에 남기지 않는다. SQL 바인드 파라미터 로그를 켜지 않는다.
- `server.error.include-message`·`include-stacktrace`는 기본값(`never`)을 유지한다.
