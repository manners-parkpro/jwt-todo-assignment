# 설계 결정 기록 (ADR-lite)

> 이후 작업에 영향을 주는 결정만 기록한다. 결정은 삭제하지 않는다.
> 상태: **확정** · **확정(실측 조건)** — 구현 단계 검증 결과에 따라 재검토 · **대체됨(→D-xx)**
> 형식: 결정 / 근거(검증 출처) / 대안. 명세 원문 사본은 `docs/assignment-spec.md`(로컬 전용).
> "초안"은 사용자가 처음 제시한 CLAUDE.md 초안(458줄)이다. 요지는 `prompts/used_prompts.md` #1에 있고, 인용한 문장은 해당 결정에 그대로 옮겼다.

| ID | 결정 | 상태 |
|---|---|---|
| D-01 | 빌드 기준선: Boot 3.5.16 · Gradle 9.8.2 · Java 17 타깃 · Tomcat 10.1.61 · Lombok 1.18.48 | 확정(S12 Lombok 보완) |
| D-02 | Lombok·springdoc 유지와 사용 범위 | 확정(S14 API 문서 설정) |
| D-03 | 기능별 헥사고날 + 경량 DDD, 포트 정책 | 확정(포트 이름·Command 범위·반환 타입은 → D-20) |
| D-04 | 도메인 모델·JPA 엔티티 분리, 집합체 간 ID 참조 | 확정 |
| D-05 | 액세스 토큰: OAuth2 Resource Server(Nimbus) + HS256, clock skew 0 | 확정 |
| D-06 | 서명 키(미설정 시 임의 키, 환경 변수 `TODO_JWT_SECRET`)·액세스 토큰 수명 1시간 | 확정(S13 이름 변경) |
| D-07 | 응답 규약: 토큰 응답 필드 명시 · ProblemDetail · 상태 코드 | 확정 |
| D-08 | 소유권 검사와 존재 은닉(타인 TODO 404) | 확정 |
| D-09 | 비밀번호·로그인 정책(BCrypt 72바이트 제한 대응) | 확정 |
| D-10 | SQLite 영속성: 테이블 생성 경로 단일화, DDL 함정 대응, 커넥션 풀 1 | 확정(S10 실측) |
| D-11 | 테스트 전략: 통합 테스트 + 실제 로그인 토큰 | 확정 |
| D-12 | 요청·응답 필드 | 확정 |
| D-13 | 작업 관리 체계, 단계 게이트, 공개 저장소 운영 | 확정 |
| D-14 | 선택 사항(가산점) 처리 정책 | 확정 |
| D-15 | 토큰 재발급(Refresh Token) 확장과 필수 테스트 보호 | 확정 |
| D-16 | 프롬프트 기록 보존(같은 턴 기록·훅 자동 백업·백필) | 확정 |
| D-17 | 지침 로드 보장과 검증(CLAUDE.md 전체 주입 확인, 로드 기록 훅) | 확정 |
| D-18 | README의 Windows 명령 범위 | 확정(S13 CI 실측) |
| D-19 | 결정적 강제(커밋·푸시 훅)와 단계별 독립 리뷰 | 확정 |
| D-20 | 계층 간 DTO(Command·Result)와 네이밍 규칙(참고 프로젝트) | 확정 |
| D-21 | 선택 사항(가산점) 설계 — 제목 검색·회원정보 수정·탈퇴(soft delete)·기능 간 이벤트 | 설계 확정(구현은 사용자 승인 후 11단계) |

---

### D-01 빌드 기준선 — 확정
- **결정**: Spring Boot **3.5.16**, Gradle Wrapper **9.8.2 유지**, Java **17 타깃**(`options.release = 17`, toolchain 없음), Tomcat **10.1.61** 오버라이드. (사용자 결정 2026-10-25)
- **근거**:
  - 명세 조건은 Java 17 이상·JUnit 5·Gradle/Maven이며 Spring Boot 버전 조건은 없다("3.x"는 초안 조건). Boot 4.1.2 BOM은 JUnit Jupiter 6.0.3, Boot 3.5.16 BOM은 5.12.2를 관리한다 → 3.5.16.
  - Gradle 공식 호환표: Gradle 8.14는 Java 24까지 실행, Java 25는 9.1.0 이상, 26은 9.4.0 이상 필요 → 채점 JDK를 모르므로 9.8.2 유지. Boot 3.5 System Requirements는 Gradle 7.6.4+/8.4+만 명시하므로 기준선 단계에서 실측한다.
  - Gradle toolchain은 같은 버전 JDK가 있어야 동작 → `release 17`로 JDK 17 이상 어디서나 빌드. 이 과제에 Java 21 전용 기능은 필요 없다.
  - 보안: 3.5 계열 OSS 지원은 2026-06-30 종료. 이후 공개된 CVE-2026-59270(임베디드 UnboundID LDAP)·CVE-2026-47842(`AesBytesEncryptor`)·CVE-2026-47886(신뢰할 수 없는 SpEL 평가)·CVE-2026-47834(Spring Data JPA 3.5.13, 네이티브 쿼리+요청 Sort)은 3.x/6.x 계열 수정이 엔터프라이즈 전용이지만, 전제 기능을 쓰지 않는다(spring.io 권고의 조건 확인) → `security.md` 사용 금지 기능으로 고정. Tomcat 10.1.56~10.1.61에는 Apache의 OSS 보안 수정이 있으므로 버전만 올린다.
- **대안**: Boot 3.5.16 + Gradle 8.14.6(공식 조합, JDK 25 이상에서 실행 불가) · Boot 4.1.2(지속 패치, JUnit 6이라 명세와 불일치) · Java 21 toolchain(채점 환경에 JDK 21 필요).
- **실측 계획**: JDK 17·21로 `./gradlew clean test/build --warning-mode all`, class major 61, `dependencyInsight tomcat-embed-core`. 실패하면 8.14.6으로 폴백하고 README에 "JDK 17~24"를 명시한다.
- **실측 결과(3단계, 2026-10-25)**: Gradle 9.8.2 + Boot 3.5.16으로 JDK 21·17 모두 `clean compileJava compileTestJava assemble`(bootJar 포함) 성공, class major 61. 해석된 버전 — spring-boot 3.5.16, spring-core 6.2.19, spring-security-oauth2-jose 6.5.11, hibernate-core·community-dialects 6.6.53.Final, sqlite-jdbc 3.49.1.0, tomcat-embed-core 10.1.61(rule로 선택), springdoc 2.8.17, junit-jupiter 5.12.2. Boot 플러그인(`JavaPluginAction`)이 `Configuration.setVisible`을 써서 deprecation 경고 1건(Gradle 11 제거 예정 — 9.x 영향 없음). 4단계에서 JDK 17·21 모두 `clean test` 통과, 실행 중 Tomcat 10.1.61 확인 → 실측 조건 해소(8.14.6 폴백 불필요).
- **실측(S11)**: 명세 "Java 17 이상"에는 상한이 없어 공개된 최신 JDK까지 확인했다.
  - 공식 범위: Gradle 9.8.2는 JDK 17~27에서 실행(28 미지원), Boot 3.5.16은 공식 호환 ~25, Boot가 관리하는 Lombok 1.18.46은 ~26.
  - 커밋본을 새 경로에 풀어 실행: JDK 17·25·26 통과, **JDK 27은 Lombok 때문에 `compileJava` 실패**(javac 내부 클래스 `EndPosTable` 제거).
  - 사본에서 Lombok 1.18.48(JDK 27 지원)로 올리면 17·21·25·26·27 모두 빌드·테스트·`bootRun`이 통과했다. 반영 여부는 사용자 협의 후 결정한다.
- **결정(사용자 2026-10-26, S12)**: `ext['lombok.version'] = '1.18.48'`로 올린다(Lombok 제거는 잃는 것이 많아 채택하지 않음 — D-02). 교차 확인 JDK는 늘리지 않고 17·21을 유지한다 — S11의 25·26·27 실측과 S12의 JDK 27 일회 확인(저장소 `clean build` 14건 통과)은 이 수정의 근거 기록이며 정기 검증이 아니다.
- **검증 정책(사용자 결정 2026-10-25)**: 기본 개발·검증은 JDK 17(타깃·명세 최소 버전), 단계 완료와 제출 전에 JDK 21로 한 번 교차 확인한다. 21 확인의 목적은 코드가 아니라 빌드 환경이다 — toolchain이 없으므로 채점자의 JDK에서 Gradle·javac·Lombok 애너테이션 프로세서·테스트 라이브러리가 실행되며, 대상이 17이어도 새 JDK에서 빌드가 깨질 수 있다(예: Lombok 1.18.28 이하는 JDK 21 javac에서 컴파일 실패, Gradle 8.14는 JDK 25에서 실행 불가).

### D-02 Lombok·springdoc 유지 — 확정
- **결정**: 둘 다 유지(사용자 결정). Lombok은 main 코드에만 쓰고 사용 범위를 제한한다(architecture.md). springdoc은 Boot 3.5용 2.8.17. Swagger 경로는 `permitAll`, `@SecurityRequirement`는 보호 API에만 붙인다.
- **근거**: 채점자가 브라우저로 API를 시험할 수 있다. springdoc 2.8.17 POM은 Boot 3.5.13 기준이다. springdoc 2.8.17의 `ObjectMapperProvider`는 자체 `Json.mapper()`를 써서 Spring의 전역 Jackson 네이밍 전략을 스키마에 반영하지 않는다(소스 확인) → D-07.
- **대안**: springdoc 제거(YAGNI 관점의 권장안이었음).
- **보완(S12)**: Boot가 관리하는 Lombok 1.18.46이 JDK 27에서 컴파일되지 않아 Lombok 제거도 검토했으나, 사용자 결정으로 유지하고 버전만 1.18.48로 올린다(D-01).
- **보완(S14, 9단계)**: 설정 전 문서는 제목 "OpenAPI definition", 보안 스키마 없음(Swagger UI에서 보호 API를 호출할 수 없음), `POST /todos` 200, 응답 형식 `*/*`였다.
  - `global.config.OpenApiConfig`에 `@OpenAPIDefinition`(제목·사용 순서·오류 형식 설명, 태그 순서 사용자 → TODO)과 `@SecurityScheme`(`bearerAuth`, HTTP bearer, JWT)을 두고, 보호 API(TODO 5개·내 정보)에만 `@SecurityRequirement`를 붙였다. 스키마 이름은 상수로 참조한다 — 이름이 어긋나면 Swagger UI가 토큰을 조용히 보내지 않는다.
  - `POST /todos`는 `ResponseEntity.created(...)`가 정하는 201을 springdoc이 알 수 없어 `@ApiResponse(responseCode = "201")`로 적는다. 오류 응답은 엔드포인트마다 적지 않고 `@Info` 설명과 README에 모은다(컨트롤러 가독성).
  - `springdoc.api-docs.enabled`·`swagger-ui.enabled: true`를 명시한다 — springdoc 2.8.17 `SpringDocAppInitializer`는 속성이 바인딩되지 않으면(기본값 `false`인 필드, 엔드포인트는 `matchIfMissing`으로 켜짐) "enabled by default" WARN을 남긴다(소스 확인, 명시 후 WARN 0건). `default-produces-media-type: application/json`으로 실제 응답 형식을 문서화한다.
  - 대안: 전역 보안 요구 + 공개 API에서 해제(`@SecurityRequirements`) — 새 API가 기본으로 보호 표시되지만 규칙(보호 API에만 붙인다)과 반대 방향이고 해제 표시가 3곳 필요하다. 대신 `ApiDocsTest`가 문서의 모든 API를 기대 목록과 비교해 빠진 표시를 잡는다.

### D-03 기능별 헥사고날 + 경량 DDD — 확정
- **결정**: `user`, `todo` 패키지마다 `domain` / `application`(`port.in`, `port.out`, `service`) / `adapter`(`in.web`, `out.persistence`, `out.security`), 공통은 `global`(`security`, `error`, `config`). In Port는 책임별 인터페이스(`UserUseCase` 가입·내 정보, `AuthUseCase` 로그인·재발급, `TodoUseCase`), Out Port는 `UserRepository`·`TodoRepository`·`RefreshTokenRepository`·`PasswordHasher`·`TokenIssuer`. Command record는 파라미터가 많은 유스케이스에만 쓴다. (포트 이름·Command 범위·반환 타입은 대체됨 → D-20)
- **근거**: 초안 §5 "저장소 및 외부 기능은 Port를 통해 접근", "도메인은 기술 세부사항에 직접 의존하지 않는다". 평가 기준 "코드를 읽기 쉬운가"와 초안의 "의미 없는 인터페이스 금지"에 따라 유스케이스별 인터페이스(9개) 대신 책임별 3개로 묶었다. 재발급 확장(D-15)으로 인증 책임이 커져 `AuthUseCase`를 계정 관리(`UserUseCase`)와 나눴다. 리프레시 토큰 생성·해시는 JDK 표준 API로 충분해 포트를 만들지 않는다.
- **보완(S7)**: application 서비스의 설정값(예: 리프레시 토큰 수명)은 생성자 파라미터 `@Value`로 받는다(application에 허용하는 유일한 추가 Spring 어노테이션). 기능 예외는 각 기능의 `application`에 두고 `global.error.GlobalExceptionHandler`가 직접 참조한다(global → 기능 예외 의존만 허용). 독립 리뷰가 5~8단계에서 의존 규칙을 어기게 될 공백으로 지적했다. 어댑터 패키지의 클래스는 package-private으로 둬서 JPA 엔티티·DTO가 포트 밖으로 새는 것을 컴파일러가 막게 한다(S9 — Spring Data·Hibernate·Jackson·검증기가 package-private 클래스에서 동작함을 테스트로 확인).
- **대안**: 유스케이스별 In Port(정석이지만 파일 증가) · In Port 생략(헥사고날 표현 약화).

### D-04 도메인 모델과 JPA 엔티티 분리, 집합체 간 ID 참조 — 확정
- **결정**: 도메인(`User`, `Todo`, `RefreshToken`)과 JPA 엔티티(`*JpaEntity`)를 분리하고 변환은 영속성 어댑터에서 한다. `Todo`·`RefreshToken`은 사용자를 ID(Long)로만 참조한다.
- **근거**: 초안 §5.5·§5.7(DTO·도메인·JPA 엔티티 책임 구분). DDD 집합체 설계 원칙 "다른 집합체는 식별자로 참조". SQLite에서는 FK가 생성되지 않아(D-10) 연관 매핑의 이점도 작다.
- **대안**: 도메인 클래스에 JPA 어노테이션 — 코드는 줄지만 도메인에 기술 의존이 들어온다.

### D-05 액세스 토큰: Spring Security OAuth2 Resource Server(Nimbus), HS256 — 확정
- **결정**: `spring-boot-starter-oauth2-resource-server`. 발급은 `NimbusJwtEncoder(ImmutableSecret)` + HS256 헤더 명시, 검증은 `NimbusJwtDecoder.withSecretKey(...).macAlgorithm(HS256)` + `JwtTimestampValidator(Duration.ZERO)`. 클레임은 `sub`(사용자 ID)·`iat`·`exp`.
- **근거**: 표준 `BearerTokenAuthenticationFilter`가 헤더 추출·서명·만료 검증·401 응답을 처리하므로 직접 만든 필터가 필요 없다(SEC-04·05·08). Spring Security 6.5.11 소스로 확인: `withSecretKey` 기본 HS256, 기본 검증기의 clock skew 60초, `BearerTokenAuthenticationEntryPoint` = 401 + `WWW-Authenticate`, `NimbusJwtEncoder` 기본 헤더 RS256(→ HS256 명시 필요). `iss`는 서버 하나가 같은 HMAC 키로 발급·검증하는 구조에서 이점이 없어 넣지 않는다(YAGNI). clock skew 60초면 만료 후에도 최대 60초 통과하므로, 시계가 같은 단일 서버 조건에서 0초로 둔다.
- **대안**: jjwt + `OncePerRequestFilter` — 추출·검증·예외 처리를 직접 구현해야 해 코드와 실수 여지가 늘어난다.

### D-06 서명 키·액세스 토큰 수명 — 확정
- **결정**: 설정은 `auth.jwt-secret: ${JWT_SECRET:}`, `auth.access-token-ttl: 1h`. `JWT_SECRET`(Base64, 디코딩 32바이트 이상)이 있으면 그 키를 쓰고, 없으면 기동 시 `SecureRandom` 32바이트 키를 만들고 WARN 로그를 남긴다. 값의 형식이 잘못되면 기동을 실패시킨다. 테스트도 환경 변수 없이 자동 생성 키를 쓴다.
- **근거**: 실격 조건 "README에 적힌 명령어로 실행되지 않는 경우" — 환경 변수를 빠뜨려도 실행된다. 초안의 "서명 키 하드코딩 금지"를 지킨다(고정 키 없음). 재시작하면 기존 액세스 토큰이 무효가 되지만 리프레시 토큰으로 다시 발급받는다(D-15, 사용자 인지 2026-10-25). RFC 8725 §3.5(충분한 엔트로피의 키) → `SecureRandom` 또는 `openssl rand -base64 32`.
- **대안**: 키 필수(fail-fast) — 운영 환경에는 더 엄격하지만 채점자가 설정을 빠뜨리면 기동이 실패하고 테스트 키 주입 코드가 필요하다. 테스트 프로파일에 더미 키 — 저장소에 키 문자열이 남는다.
- **보완(S13, 실격 위험 점검)**: 환경 변수 이름을 `JWT_SECRET` → `TODO_JWT_SECRET`으로 바꿨다(`auth.jwt-secret: ${TODO_JWT_SECRET:}`).
  - 이유: 같은 과제를 받은 다른 지원자들도 흔히 `JWT_SECRET`을 쓴다. 채점자가 여러 제출물을 한 셸에서 실행하면 다른 형식의 값이 남아 있을 수 있고, 그러면 우리 형식 검사(Base64·32바이트)로 README 명령이 기동에 실패한다 — `JWT_SECRET='my-jwt-secret-for-another-project'`로 실측.
  - 결과: 전용 이름이 아닌 값은 무시하고 임의 키로 기동하며(실측), 전용 이름에 잘못된 값을 넣은 경우에만 기동을 멈춘다(명시적 설정 오류는 숨기지 않는다).
  - 사용자 결정(S13): TTL(`access-token-ttl`·`refresh-token-ttl`)은 `@Value` 기본값이 아니라 yml에 둔다(현행 유지).

### D-07 응답 규약 — 확정
- **결정**: 로그인·재발급 응답은 `{"access_token": ..., "refresh_token": ...}`이고 두 필드에 `@JsonProperty`로 snake_case를 명시한다(사용자 결정 2026-10-25). 다른 필드는 모두 한 단어이며, 여러 단어 필드를 추가하면 같은 방식으로 명시한다. 오류는 RFC 9457 ProblemDetail(401 포함), 상태 코드는 `.claude/rules/api.md` 표(201+Location·200·204·400·401·404·409)를 따른다. 모든 401(토큰 오류·로그인·재발급 실패)에 `WWW-Authenticate: Bearer`를 넣는다(RFC 9110 §15.5.2 MUST — S7 보완). `@JsonProperty`는 응답뿐 아니라 재발급 요청 record에도 붙인다. `detail`은 직접 작성하는 메시지만 한국어로 쓰고, Spring MVC 표준 예외의 기본 `detail`은 영문 그대로 둔다(S8 리뷰 — 초안·명세에 언어 조건이 없고 기본값에 내부 정보가 없음을 확인. 바꾸려면 `messages.properties`의 `problemDetail.<예외 클래스>` 코드를 쓴다).
- **보완(S12)**: 검증 실패 응답의 `errors[].field`는 클라이언트가 보낸 JSON 이름으로 쓴다 — Bean Validation은 자바 이름(`refreshToken`)을 돌려주므로 `GlobalExceptionHandler`가 Jackson의 속성 정보로 `refresh_token`으로 바꾼다(한 단어 필드는 그대로).
- **근거**: 명세의 `access_token` 표기를 그대로 지키고 확장 필드만 더한다(RFC 6749 §5.1 토큰 응답 관례). 전역 `SNAKE_CASE`는 springdoc 스키마에 반영되지 않아(D-02) 필드 단위 명시가 단순하고 정확하다. 초안 §3.5 "명세 외 응답은 프로젝트 전체에서 일관". ProblemDetail은 Spring Framework 6 내장이라 추가 의존성이 없다.
- **대안**: 전역 SNAKE_CASE + springdoc용 `ModelResolver` 빈 — 설정이 두 곳으로 나뉜다. 리프레시 토큰을 HttpOnly 쿠키로 전달 — D-15 대안 참조.

### D-08 소유권 검사와 존재 은닉 — 확정
- **결정**: 단건 조회·수정·삭제는 `id + userId` 조건으로 조회하고, 미존재·타인 소유 모두 404로 응답한다.
- **근거**: 명세 "로그인한 사용자 본인의 TODO만 다룹니다". RFC 9110 §15.5.4: 대상의 존재를 숨기려는 서버는 403 대신 404로 응답할 수 있다. IDOR 방지.
- **대안**: 403 — 다른 사용자의 리소스가 존재한다는 사실이 드러난다.
- **보완(S10)**: SQLite PK는 AUTOINCREMENT가 없어 삭제된 마지막 ID가 다른 사용자의 새 TODO에 다시 쓰일 수 있다(Hibernate `SQLiteIdentityColumnSupport`, sqlite3 재현). `id + userId` 조건이 이때도 타인 데이터를 막는다. 이 동작은 명세의 필수 요구("본인의 TODO만")라 검증(TEST-X2)을 10단계에서 7단계로 앞당겼다.

### D-09 비밀번호·로그인 정책 — 확정
- **결정**: `DelegatingPasswordEncoder`(bcrypt). 비밀번호 8~64자 + UTF-8 72바이트 이하, username 중복은 409, 로그인 실패는 하나의 401 메시지.
- **근거**: Spring Security 6.5.11 `BCrypt.java:615-616`이 72바이트 초과 시 `IllegalArgumentException("password cannot be more than 72 bytes")`을 던진다 → 검증하지 않으면 500(CVE-2025-22228 대응 이후 동작). 회원가입 409가 이미 계정 존재를 드러내므로 로그인 응답 시간 균등화(더미 해시 비교)는 하지 않는다(YAGNI). 메시지 통일은 비용이 없어 유지한다.
- **보완(S9)**: 72바이트 예외는 인코딩(`for_check=false`)에서만 나고 비교는 예외 없이 동작하므로(6.5.11 소스), 로그인 요청은 필수 여부만 검사하고 가입 규칙(길이)은 적용하지 않는다 — 틀린 값은 401. 검증 메시지는 어노테이션에 고정한 한국어다(기본 메시지는 요청 로캘·JVM 로캘에 따라 달라 채점 환경마다 응답이 바뀐다). `PasswordEncoder`는 빈으로 등록하지 않고 해시 어댑터 안에서만 만든다(사용처가 하나).

### D-10 SQLite 영속성 — 확정(S10 실측)
- **결정**: 테이블 생성 경로는 README 명령(`./gradlew bootRun`, `./gradlew test`)이 일으키는 Hibernate `ddl-auto` 하나다(앱 `update`, 테스트 `create-drop`). 수동 SQL·`schema.sql`·`data.sql`·마이그레이션 명령·미리 만든 DB 파일과 README의 SQL 명령을 금지한다(사용자 요청 2026-10-25). dialect 명시, `open-in-view: false`, Hikari `maximum-pool-size: 1`, DB 파일은 앱 `./todo.db`·테스트 `build/todo-test.db`. FK 대신 사용자 ID 컬럼(`todos.user_id`, `refresh_tokens.user_id`) 인덱스 + 애플리케이션 무결성, 유니크는 `@Column(unique = true)`. 사용자 ID 필드는 두 테이블 모두 `userId`다(S8 사용자 검토 — S7에 컬럼을 `owner_id`로 바꿨던 것을 되돌림: Boot 기본 명명 전략(`CamelCaseToUnderscoresNamingStrategy`, 3.5.16 jar 확인)이 두 이름 모두 어노테이션 없이 바꿔 주므로 매핑 이점은 같고, 관례·명세 용어("사용자 본인의 TODO")·JWT `sub`부터 저장소까지 같은 이름·`refresh_tokens`와의 일관성에서 `userId`가 낫다. `ownerId`의 이점은 사용자 관계가 여럿일 때의 구분인데 범위 밖이다). 컬럼 타입은 Long·String·boolean이고 시각은 epoch 밀리초 Long. 네이티브 쿼리·요청 Sort는 쓰지 않는다.
- **근거**: 명세 "README에 적힌 명령어만으로 테이블이 생성되어야 합니다. (지원자가 SQL을 직접 실행해야 하는 방식은 안 됩니다)". 생성 경로를 하나로 두면 채점 환경에서 결과가 결정적이다. Boot 3.5.16 BOM — sqlite-jdbc 3.49.1.0, hibernate-community-dialects = Hibernate 6.6.53.Final. 소스로 확인 — SQLiteDialect `hasAlterTable()=false` → `StandardForeignKeyExporter`가 FK DDL을 만들지 않는다. SQLite용 유니크 처리는 컬럼 정의에 `unique`를 인라인으로 붙이고, ALTER가 필요한 테이블 단위 유니크 제약은 생략한다. `busy_timeout` 기본 3000ms, SQLite는 동시 writer 1개.
- **위험**: 풀 1개에서 연결 대기 타임아웃이 생기면 기본 풀 + busy_timeout 방식으로 재검토한다.
- **실측(S10)**: `bootRun`에서 TODO API 병렬 요청 120건(POST 60·GET 60, 동시 30)이 모두 성공했고 타임아웃·`SQLITE_BUSY`가 없었다 → 실측 조건 해소. 풀 1개라 트랜잭션이 커넥션 순서대로 처리돼 "중복 확인 → 저장" 사이의 경쟁도 생기지 않는다.
  - 한 트랜잭션 안에서 두 번째 커넥션을 요구하는 코드(`REQUIRES_NEW`)는 쓰지 않는다. 바깥 트랜잭션이 커넥션을 쥔 채 기다리다 Hikari `connectionTimeout`(30초) 뒤 예외로 실패하고, 풀을 늘려도 바깥 트랜잭션의 SQLite 잠금 때문에 안쪽 커밋이 `SQLITE_BUSY`로 실패한다(D-15 보완, S10 리뷰).
  - 관찰(S10 리뷰): 가입·로그인은 bcrypt 계산을 트랜잭션 안에서 해 그동안 커넥션 1개를 쥔다 → 로그인이 몰리면 다른 요청이 기다린다. 채점 시나리오에는 영향이 없어 바꾸지 않는다.

### D-11 테스트 전략 — 확정
- **결정**: `@SpringBootTest` + MockMvc + `test` 프로파일. 보안 필터를 포함하고 실제 `/users/login` 토큰을 쓴다. 고유 username으로 격리한다. `spring-security-test`는 넣지 않는다.
- **근거**: 필수 시나리오가 HTTP 흐름(가입→로그인→조회 등)이고 명세는 단위·통합 방식을 자유로 둔다. 통합 테스트가 핵심 흐름의 직접적인 증거이며, 실제 토큰을 쓰므로 목 인증 라이브러리가 필요 없다.

### D-12 요청·응답 필드 — 확정
- **결정**: 가입·로그인 `username`, `password` / 내 정보 `{id, username}` / 재발급 요청 `refresh_token` / TODO `title`(필수), `description`(선택), `completed`(기본 false) / 목록은 배열·id 오름차순 / PUT은 전체 교체.
- **근거**: 명세 원문(2026-10-25 확인)에 필드 정의가 없어 최소 필드로 정했다. 명세 선택 기능의 "제목으로 검색"과 `title`이 맞는다.

### D-13 작업 관리 체계, 단계 게이트, 공개 저장소 운영 — 확정
- **결정**:
  - 문서 체계: `CLAUDE.md`(상시 로드) + `.claude/rules/` 7개(경로 한정 자동 로드) + `.claude/skills/submission-check`(사용자가 실행하는 제출 절차) + `.claude/agents/step-reviewer.md`(단계 리뷰, D-19) + `docs/`(체크리스트·결정·진행, 명세 사본) + `prompts/used_prompts.md`(명세대로 "간단히").
  - 진행: 설계 승인(2026-10-25, used_prompts #2) → 단계별 계획·구현·검증·리뷰·보고 → 사용자 확인 후 다음 단계.
  - 공개 저장소 `https://github.com/manners-parkpro/jwt-todo-assignment.git`(`main`): 명세 사본 `docs/assignment-spec.md`는 `.gitignore`로 제외하고 로컬·제출 ZIP에만 둔다(사용자 결정). 도구(memory-bank MCP)가 다시 만든 `memory-bank/`는 로컬에 남기고 `.gitignore`와 ZIP 제외 목록으로 뺀다(사용자 결정 S12).
  - 커밋·푸시: 단계 검증과 문서 갱신을 마친 뒤 커밋하고 푸시한다(사용자 지시 2026-10-25 "nickname 만 부착하여 commit + push"). 강제 푸시·저장소 재생성 같은 원격 기록 덮어쓰기는 사용자가 직접 한다.
  - 작성자: 저장소 로컬 git 설정의 닉네임 `Colin` + GitHub noreply 주소(회사 도메인 제외 — 사용자 요청). 초기 커밋은 작성자만 바꿔 다시 만들었고, 사용자가 저장소를 재생성해 이전 커밋은 조회되지 않는다(HTTP 409).
  - 푸시 인증: 저장소 로컬 자격 증명 헬퍼가 `gh auth token --user manners-parkpro`를 쓴다(사용자 결정 C안, 설정은 `docs/progress.md` 환경 메모). gh 기본 헬퍼는 활성 계정 토큰만 반환하므로(gh 2.90.0 소스), 전역 계정 전환 없이 이 저장소만 개인 계정으로 푸시한다.
- **근거**: Claude Code 공식 문서(memory·best-practices·skills) — CLAUDE.md는 200줄 미만 권장, 일부 영역 규칙은 path-scoped rule(Read/Write/Edit 시 로드), 다단계 절차는 skill, `@import`는 컨텍스트를 줄이지 못함, 자명한 지침은 제외. Anthropic "Effective harnesses for long-running agents" — 진행 로그·기능 목록·git, 세션 시작 시 기준선 확인, 한 번에 한 기능. 회사 과제 명세의 공개 재배포는 피한다.
- **대안**: 단일 CLAUDE.md(초안 458줄) — 권장 크기를 넘어 지침 준수율이 떨어진다 · 전역 `gh auth switch` — 전환 중 다른 저장소에 영향 · 다른 계정에 쓰기 권한 부여 — 푸시 계정이 저장소 활동 기록에 남는다.

### D-14 선택 사항(가산점) 처리 — 확정
- **결정**: OPT-01~03(검색, 회원 수정·탈퇴)은 필수 테스트 TEST-01~04, 확장 테스트 TEST-07·08, 선택 테스트 TEST-X를 마친 뒤(10단계 이후) 범위와 위험을 다시 판단해 결정한다(11단계). README는 그 결정 뒤 최종 API 기준으로 작성·실검증한다(12단계 — S7에서 README와 순서를 바꿨다). 추가 테스트(TEST-X)는 필수 4종 완료 후 작성한다.
- **근거**: 명세 "안 해도 감점 없음", 평가 1순위는 요구사항대로 동작하는가, YAGNI. README를 먼저 쓰면 선택 기능을 넣을 때 API 목록과 실행 검증을 다시 해야 한다(독립 리뷰 지적). 회원 수정(비밀번호 변경)과 탈퇴는 리프레시 토큰 폐기와 TODO 정리(FK 없음) 같은 추가 결정이 필요하다.
- **판단 항목(S10)**: 탈퇴(OPT-03)를 고르면 액세스 토큰은 만료(1시간) 전까지 유효해 탈퇴 뒤에도 TODO API를 호출할 수 있다 — FK가 없어 주인 없는 행이 생긴다. 기능 간 직접 참조 금지(`architecture.md`)와 함께 판단한다.
- **보완(S14, 사용자 지시 #13)**: 지금까지는 처리 시점만 있고 API·규칙·테스트 설계가 없었다 → 가산점을 위해 OPT-01~03을 모두 설계했다(D-21). 구현 여부·순서는 사용자 승인을 받는다(CLAUDE.md 단계 게이트). 명세의 세 번째 선택 사항 "필수 시나리오 외의 추가 테스트"는 TEST-07·08·09·X2·API-10이 있고 TEST-X1·X3·X4가 10단계 계획이다.

### D-15 토큰 재발급(Refresh Token) 확장과 필수 테스트 보호 — 확정
- **결정**:
  - 로그인 때 액세스 토큰(JWT, 1시간)과 리프레시 토큰을 함께 발급하고 응답 본문 `{access_token, refresh_token}`으로 전달한다(사용자 결정 2026-10-25).
  - 리프레시 토큰은 `SecureRandom` 32바이트 Base64URL 불투명 토큰이다. SHA-256 해시만 `refresh_tokens`에 저장하고, 만료는 `auth.refresh-token-ttl: 7d`다.
  - `POST /users/refresh`가 유효한 리프레시 토큰을 새 토큰 쌍으로 교환한다(회전 — 사용한 토큰은 `used` 표시).
  - 이미 사용한 토큰이 다시 오면 그 사용자의 리프레시 토큰을 모두 폐기한다(재사용 탐지). 없는 토큰·만료·재사용은 같은 401로 응답한다.
  - 공개 인증 엔드포인트(가입·로그인·재발급)는 `Authorization` 헤더를 무시하는 `BearerTokenResolver`를 쓴다.
  - **필수 테스트 보호**(사용자 지시 2026-10-25 "필수 테스트 사항에 대한 제약이나 누락이 되어서는 결코 안 된다"): TEST-01~04는 리프레시 토큰과 독립이다(TEST-01은 `access_token`만 사용). 6단계에서 로그인을 `access_token`만 발급하도록 완성해 필수 흐름을 먼저 통과시키고, 8단계에서 `refresh_token` 발급과 재발급 API를 추가한 뒤 TEST-01~04 회귀를 확인한다.
- **근거**: 사용자 요청 "토큰이 만료되었을 때 재요청 시 다시 발급하여 API 호출이 가능하도록". 리프레시 토큰을 JWT가 아닌 DB 저장 불투명 토큰으로 두면 서명 키와 무관해서, 임의 키 대체(D-06)로 재시작한 뒤에도 재발급된다. RFC 9700 §2.2.2: 공개 클라이언트의 리프레시 토큰은 sender-constrained이거나 회전을 써야 한다(MUST). §4.14.2: 회전 시 이전 토큰은 무효화하되 관계를 보관하고, 무효화된 토큰이 제출되면 침해로 보고 활성 토큰을 폐기한다. 해시만 저장하면 DB가 유출돼도 토큰을 쓸 수 없다. 리소스 서버는 permitAll 경로에서도 Bearer 헤더를 검증하므로, 공개 인증 경로에서 헤더를 무시해야 만료 토큰을 단 클라이언트가 재발급을 받을 수 있다. 명세는 Refresh Token을 "구현하지 않아도 된다"고만 하므로 구현해도 충돌하지 않고, `access_token` 필드는 그대로다.
- **대안**: HttpOnly 쿠키 전달(명세 응답과 정확히 일치하지만 curl·테스트에 쿠키 처리 필요) · JWT 리프레시 토큰(서명 키에 묶여 재시작 시 무효, 폐기 불가) · 재로그인만 허용(확장 요구 미충족) · 토큰 계열(family) 단위 폐기(더 정밀하지만 컬럼·로직 추가 — 사용자 단위 폐기로 단순화, 대신 재사용 탐지 시 그 사용자의 다른 기기도 다시 로그인해야 함).
- **범위 밖(YAGNI)**: 로그아웃 엔드포인트, 리프레시 토큰 sender-constraint(DPoP·mTLS), 재발급 요청 속도 제한.
- **보완(S12, 구현 전 재검토)**:
  - TEST-09 추가: 계획된 TEST-07·08은 회전·재사용만 확인해 "7일 만료" 규칙을 검증하는 테스트가 없었다 → 수명 0초 컨텍스트(전용 DB 파일)에서 만료·없는 토큰이 같은 401인지 확인한다.
  - 필수 테스트 보호: 테스트 도우미를 access_token 전용과 확장용으로 나누고, 로그인 응답의 `refresh_token`을 null로 바꾼 변이에서 TEST-01~04가 통과함을 확인했다.
  - 해시는 SHA-256을 유지한다. HMAC(서버 키)은 서명 키(`TODO_JWT_SECRET`) 미설정 시 재시작마다 키가 바뀌어 리프레시 토큰까지 무효가 되므로 확장 목적(재시작 후 재발급)과 충돌한다. 256비트 난수라 SHA-256만으로 추측할 수 없다.
  - 만료 기준은 "현재 ≥ 만료 시각"이고, 로그인 때의 정리 쿼리도 같은 기준(`LessThanEqual`)이다.
  - 알려진 한계(수용): 재사용 탐지로 폐기해도 이미 발급된 액세스 토큰은 만료까지 최대 1시간 유효하다(무상태 JWT). 같은 리프레시 토큰으로 동시에 재발급하면 재사용으로 판정해 그 사용자의 리프레시 토큰을 모두 폐기한다(RFC 9700의 엄격한 동작). 재발급마다 7일이 다시 시작되며 전체 세션 수명 상한은 두지 않는다.
- **보완(S10)**: 재사용 탐지로 폐기한 뒤 401 예외를 던지면 Spring 기본 롤백 규칙(RuntimeException)으로 폐기가 취소된다 → 재발급 메서드는 `@Transactional(noRollbackFor = …)`로 폐기를 커밋하고, 예외는 그 메서드 안에서 직접 던진다. 같은 트랜잭션에 참여하는 다른 `@Transactional` 경계를 거쳐 나오면 그 경계가 트랜잭션을 rollback-only로 표시해 커밋 때 `UnexpectedRollbackException`(500)이 난다(S10 리뷰). `REQUIRES_NEW`로 분리하지 않는다(D-10 — 30초 대기 뒤 실패). TEST-08("재사용 뒤 최신 토큰도 401")로 검증한다.

### D-16 프롬프트 기록 보존 — 확정
- **결정**:
  - `prompts/used_prompts.md`(제출용 요약 기록)는 프롬프트를 받은 턴 안에 갱신한다(커밋·푸시는 D-13). 기록 대상은 주요 사용자 프롬프트이고 슬래시 명령·하네스 알림은 제외한다.
  - `UserPromptSubmit` 훅(`.claude/hooks/log-prompt.py`, 설정은 로컬 `.claude/settings.local.json`)이 제출되는 프롬프트 원문을 `.claude/prompt-history.jsonl`(권한 600, 공개 저장소·ZIP 제외)에 자동으로 덧붙인다. 같은 이벤트로 들어오는 하네스 메시지(`<task-notification>` 백그라운드 작업 알림, `<agent-message` 서브에이전트 보고)는 기록하지 않는다(S7 — 그 전에 기록된 4건은 대조할 때 건너뛴다).
  - 이번 세션 프롬프트 7건은 `~/.claude/history.jsonl`과 붙여넣기 캐시에서 원문을 복원해 같은 파일에 백필했다.
  - 새 세션은 시작할 때 백업 파일과 `used_prompts.md`를 대조해 빠진 기록을 먼저 채운다(CLAUDE.md 세션 루틴).
- **근거(검증)**:
  - 컨텍스트 초기화·압축은 디스크 파일을 지우지 않지만, 대화 속 원문은 압축 때 요약되고 초기화 후에는 다음 세션의 맥락에 없다(Claude Code 문서 "What survives compaction": 프로젝트 루트 CLAUDE.md는 디스크에서 다시 주입, 대화는 요약).
  - 트랜스크립트(`~/.claude/projects/…/*.jsonl`)와 `paste-cache/`는 `cleanupPeriodDays`(기본 30일)가 지나면 자동 삭제된다. `history.jsonl`은 남지만 큰 붙여넣기는 해시 참조만 저장한다 — 실측 결과 첫 프롬프트의 초안 원문(18.7KB)은 `paste-cache`에만 있었다.
  - 훅은 프롬프트 제출마다 실행되며 입력의 `prompt` 필드로 원문을 받는다. stdout은 컨텍스트에 추가되고 종료 코드 2는 프롬프트를 막으므로, 출력 없이 기록하고 실패하면 1로 끝낸다(경고만 표시). 설정 파일 변경은 파일 감시자가 자동으로 반영한다. 이 훅은 예약 작업·서브에이전트 보고·다른 세션 메시지에도 실행되고 입력에 출처를 구분하는 필드가 없다(공식 문서 hooks) → 접두어로 거른다.
- **검증**: 파이프 테스트(정상: 종료 0·stdout 없음·권한 600·원문 일치·덧붙이기 / 실패: 종료 1), jq 스키마 검증, 셸 형식 명령 실행, 백필 7건 중 복원 실패 0건. 실제 실행: S5(이후 프롬프트 기록), S7(압축 뒤 첫 프롬프트 기록, 하네스 메시지 제외 파이프 테스트).
- **대안**: `cleanupPeriodDays` 상향(전역 설정이라 모든 프로젝트에 영향, 붙여넣기 캐시만으로 남는 문제는 그대로) · 원문 로그를 공개 저장소에 커밋(프롬프트 원문 전체 공개).

### D-17 지침 로드 보장과 검증 — 확정
- **결정**:
  - CLAUDE.md는 하네스가 세션 시작과 압축 직후 디스크에서 전체를 읽어 주입하므로, 다시 주입하는 훅(SessionStart에서 CLAUDE.md 출력 등)은 두지 않는다 — 같은 내용이 두 번 들어가 컨텍스트만 늘어난다.
  - 경로 규칙은 압축·새 세션 뒤 다시 로드될 때까지 빠져 있으므로 CLAUDE.md 원칙 8(새 파일 전에 rules 읽기, 규칙 대상 파일은 Read·Edit·Write 도구로)로 보완한다.
  - 로드 증거: `InstructionsLoaded` 훅(`.claude/hooks/log-instructions.py`, 설정은 로컬)이 로드 이유(`session_start`·`compact`·`path_glob_match` 등)·파일·글자 수·sha256 앞 12자리·트리거 파일을 `.claude/instructions-log.jsonl`(권한 600, 공개 저장소·ZIP 제외)에 남긴다. 사용자는 `/context`의 Memory files나 `/memory`로도 확인한다.
- **근거**: 공식 문서(memory) — 시작 시 로드되는 CLAUDE.md는 4 MiB 이하면 전체가 들어가고 블록 단위 HTML 주석은 주입 전에 제거된다. 압축 뒤 프로젝트 루트 CLAUDE.md는 디스크에서 다시 읽어 주입하고, `paths` 규칙과 하위 디렉터리 CLAUDE.md는 필요할 때 다시 로드한다. 필요할 때 로드되는 파일에는 파일당 글자 수 제한(200K 컨텍스트 40,000자)이 있다 — 현재 rules는 1,496~3,108자. 공식 문서(hooks) — `InstructionsLoaded`는 비동기 관찰용이라 로드를 막거나 바꾸지 않는다.
- **검증(S7)**:
  - 압축 뒤 트랜스크립트에 주입된 CLAUDE.md 본문(4,820자)과 디스크 파일을 비교 — 차이는 유지보수용 HTML 주석 3줄과 끝 줄바꿈뿐(설계대로 제거됨). 독립 리뷰 서브에이전트도 본문 전체가 로드됐음을 확인했다.
  - 압축 뒤 `docs/` 파일을 읽자 `docs.md`가 다시 로드됐다. rules 파일을 Read 도구로 직접 읽은 뒤에는 같은 규칙이 다시 주입되지 않았다(이미 컨텍스트에 있음). 새 컨텍스트 서브에이전트에서는 `build.gradle`·Java 파일을 읽자 `build.md`·`architecture.md`·`security.md`가 로드됐고, 훅이 세 건을 `path_glob_match`로 기록했다(해시가 디스크 파일과 일치).
  - 이 세션에서 세션 중에 만든 CLAUDE.md·rules는 압축 전까지 한 번도 주입되지 않았다(트랜스크립트) → 지침 파일을 새로 만들거나 크게 고치면 다음 압축·새 세션 전까지는 반영을 가정하지 않는다.
- **확인(S10)**: 압축 직후 훅이 `compact` 이유로 CLAUDE.md 5,753자·해시 `6a1435fe2e14`를 기록했고, 커밋본과 글자 수·해시가 같다. **미확인**: `session_start` 이유의 기록은 다음 새 세션에서 확인한다.
- **대안**: SessionStart 훅으로 CLAUDE.md 재출력(중복 주입) · 훅 없이 `/context`만 사용(사용자 화면에서만 확인되고 기록이 남지 않음).

### D-18 README의 Windows 명령 범위 — 확정
- **결정**: README 명령은 macOS/Linux 기준으로 실측한 것만 적고, Windows는 Gradle Wrapper 표준 명령(`.\gradlew.bat bootRun`, `.\gradlew.bat test`)만 함께 적는다. PowerShell 키 생성 같은 추가 명령은 적지 않는다(`TODO_JWT_SECRET`은 선택).
- **근거**: 실격 조건 1(README 명령으로 실행되지 않음)과 "실행해 확인한 명령만 적는다"는 규칙이 충돌한다 — 이 PC는 macOS라 Windows 명령을 실행할 수 없다(독립 리뷰 지적). Wrapper 스크립트는 Gradle이 생성한 표준 파일이고 같은 태스크를 호출하므로 위험이 가장 낮다. sqlite-jdbc 3.49.1.0 jar에 Windows 네이티브 라이브러리(x86·x86_64·aarch64·armv7)가 들어 있음을 확인했다(S7).
- **대안**: Windows 명령 생략(Windows 채점자 불편) · CI(Windows 러너)로 실측(범위 밖 — 필요하면 사용자 결정 → S13에서 채택).
- **보완(S13, 사용자 결정 "GitHub Actions로 실측")**: `.github/workflows/ci.yml`이 push마다 Windows·Linux·macOS × JDK 17·21(Temurin)에서 README 명령을 그대로 실행한다.
  - 테스트: `./gradlew test`(Windows는 PowerShell에서 `.\gradlew.bat test`).
  - 실행: `todo.db`가 없는 상태에서 `./gradlew bootRun`(Windows `.\gradlew.bat bootRun`)만으로 띄운 뒤 `.github/ci/ReadmeSmoke.java`(JDK 단일 파일 실행, ASCII 소스)가 가입 201 → 로그인 200 → 내 정보 200 → 토큰 없는 목록 401 → TODO 생성 201·목록 200·수정 200·삭제 204 → 삭제 뒤 조회 404(한국어 detail 보존) → 재발급 200을 확인한다. 테이블이 있어야 성공하는 호출이므로 실행 명령만으로 테이블이 생김을 함께 보인다(Linux·macOS는 `sqlite3 .schema`로 테이블 3개·인덱스 2개 출력).
  - 결과: 커밋 `5e37ef0` 실행(run 40277959532)에서 6개 조합 모두 성공 — 각 작업에서 `> Task :test` 실행·BUILD SUCCESSFUL, 스모크 10개 호출이 기대 상태 코드와 일치. Windows에서도 404 응답의 한국어 detail이 그대로였다(소스 컴파일 인코딩 문제 없음 — Gradle이 javac에 `-encoding UTF-8`을 넘기는 것은 로컬 디버그 로그로 확인).
  - 같은 실행에서 Windows 작업만 작업 뒤 캐시 저장이 경고로 실패했다(`fileHashes.lock … Device or resource busy` — 실행 중인 서버의 Gradle 데몬이 잠금 파일을 쥠). 검증 결과와는 무관하지만, 서버를 멈추는 단계를 로그 보관 전에 두었다.

### D-19 결정적 강제(커밋·푸시 훅)와 단계별 독립 리뷰 — 확정
- **결정**:
  - 예외 없이 지켜야 하는 규칙은 지침 대신 훅으로 강제한다. `PreToolUse`(Bash) 훅 `.claude/hooks/guard-git.py`가 `git commit` 전에 작성자 덮어쓰기·저장소 로컬 `user.email`(GitHub noreply 접미사 허용 목록)·JDK 17 `./gradlew test` 통과를, `git push` 전에 강제 푸시 여부와 올라갈 커밋의 작성자·커미터 주소를 검사하고 위반이면 거부한다. 설정은 로컬 `.claude/settings.local.json`.
  - 구현 단계는 완료 보고 전에 `.claude/agents/step-reviewer.md`(읽기 전용 서브에이전트)가 새 컨텍스트에서 변경분을 명세·체크리스트·rules와 대조한다. 정확성·요구사항·보안·범위 지적만 반영하고 나머지는 보고만 한다.
- **근거**: Claude Code best practices — "Use hooks for actions that must happen every time with zero exceptions"(지침은 권고, 훅은 결정적), "Add an adversarial review step … flag only gaps that affect correctness or the stated requirements"(과잉 설계 방지). 사용자 요구 — 필수 테스트는 "결코" 제약·누락되면 안 되고, 회사 도메인이 커밋에 남으면 안 된다(D-13·D-15). 회사 정보를 파일에 적지 않도록 차단 목록이 아니라 noreply 허용 목록으로 검사한다.
- **검증(S7)**: 파이프 테스트 15건 — 허용(일반 명령, 강제가 아닌 푸시, 실제 테스트를 실행한 커밋 10초) / 거부(`--author`·`-c user.email`·환경 변수 덮어쓰기, `--force`·`--force-with-lease`·`+ref`, noreply가 아닌 설정·커밋, 실패하는 테스트). 설정 뒤 무해한 `echo`로 실제 거부를 확인했다. `step-reviewer`는 세션 중에 만들어 5단계에서 처음 사용한다(미실행).
- **한계**: 사용자가 `!`로 직접 실행하는 명령과 Claude Code 밖의 git에는 적용되지 않는다. 명령 문자열을 검사하므로 `echo` 안의 같은 문자열도 막는다(안전한 쪽 오탐). 훅 자체가 실패하면(종료 1) 막지 않고 경고만 남긴다.
- **대안**: Stop 훅으로 매 턴 테스트(문서만 바꾼 턴에도 실행되고 사용자 확인 단계와 충돌) · 지침만 유지(권고라 누락 가능).

### D-20 계층 간 DTO(Command·Result)와 네이밍 규칙 — 확정
- **결정**(사용자 지시 2026-10-25, S10):
  - 유스케이스는 도메인 객체 대신 Result record(`UserResult`·`TokenResult`·`TodoResult`, `application/dto`)를 반환하고, 웹 어댑터는 `Response.from(result)`로 변환한다. 웹 어댑터는 도메인을 참조하지 않는다.
  - 요청 본문이 있는 쓰기 유스케이스는 Request의 `toCommand(...)`로 만든 Command(`SignupCommand`·`LoginCommand`·`CreateTodoCommand`·`UpdateTodoCommand`)를 받는다. 경로·인증 값만 쓰는 조회·삭제는 값을 직접 받는다.
  - 네이밍은 사용자가 지정한 참고 프로젝트(비공개 — 이름·경로는 공개 기록에 남기지 않는다)의 규칙을 따른다. Out Port는 영속화 `<대상>CommandPort`·`<대상>QueryPort`, 그 외 `<대상>Port`(`PasswordHashPort`·`AccessTokenPort`). 어댑터는 Spring Data `<대상>Repository`를 주입하는 `<대상>RepositoryAdapter`, 그 외 `<대상>Adapter`(`PasswordHashAdapter`·`AccessTokenAdapter`). 여러 줄 인스턴스화는 인자를 한 줄에 하나씩 쓴다.
  - 참고 프로젝트와 다른 점: Request/Response는 `adapter/in/dto`로 나누지 않고 `adapter/in/web`에 둔다(나누면 public이 돼 package-private 경계(D-03)가 깨진다). JPA 엔티티는 도메인과 분리돼 있어(D-04) `<대상>JpaEntity`로 구분한다.
- **근거**: 사용자 지적 — Response가 도메인을 직접 변환하면 웹 표현이 도메인 모델에 묶여(도메인 필드 변경이 API 응답까지 번진다) 결합도가 높고, 초안 §5.7 "SOLID 원칙을 고려한다"에 어긋난다. 이 원칙은 CLAUDE.md를 압축할 때 빠져 있었다 → 작업 원칙 6에 복원했다. Result를 두면 웹은 application의 출력 계약에만 의존하고(DIP), 도메인 변경과 응답 변경의 이유가 나뉜다(SRP). 영속화 포트를 읽기·쓰기로 나누면 서비스가 쓰는 연산에만 의존한다(ISP).
- **비용**: record 6개·포트 2개 증가(user·todo). API 동작은 같고 HTTP 통합 테스트로 회귀를 확인한다.
- **대안**: 도메인 반환 유지(파일은 적지만 웹 ↔ 도메인 결합) · Request/Response까지 참고 구조대로 분리(package-private 경계 포기).

### D-21 선택 사항(가산점) 설계 — 설계 확정(구현은 사용자 승인 후 11단계)
- **공통**: 필수 API·TEST-01~04를 바꾸지 않는다(D-15와 같은 보호). 새 API의 401 확인도 TEST-03을 고치지 않고 각 기능 테스트에 둔다. 기능마다 구현 → 테스트 → TEST-01~04 회귀 → `ApiDocsTest` 목록 갱신 → `bootRun` 확인을 한 번에 하나씩 한다(OPT-01 → 02 → 03, 스키마 변경이 없는 것부터).
- **OPT-01 제목 검색** `GET /todos/search?keyword=`(인증) → 200 `[Todo]`:
  - 본인 TODO 중 제목에 검색어가 들어간 것만 id 오름차순으로 돌려준다(없으면 `[]`). ASCII 대소문자는 구분하지 않고, `%`·`_`는 문자 그대로 찾는다 — Spring Data JPA 3.5.13의 `Containing`이 값을 `\`로 이스케이프하고 `like … escape '\'`로 질의한다(소스 확인).
  - 입력: `keyword` 필수·공백 불가·100자 이하(제목 최대 길이). 요청 record `TodoSearchRequest`를 `@Valid @ParameterObject`로 받는다 — 레코드 `@ModelAttribute`의 검증 실패는 `MethodArgumentNotValidException`이라(Spring 6.2.19 소스 확인) 기존 오류 형식(`errors[].field = keyword`)을 그대로 쓴다.
  - `/todos/search`는 리터럴 경로라 `/todos/{id}`보다 먼저 매칭된다.
  - 구현: `TodoUseCase.search(userId, keyword)`, `TodoQueryPort.findAllByUserIdAndTitleContaining`, `TodoRepository.findAllByUserIdAndTitleContainingIgnoreCaseOrderByIdAsc`.
- **OPT-02 회원정보 수정** `PUT /users/me`(인증) → 200 `{id, username}`:
  - 본문 `{"username", "current_password", "new_password"(선택)}`. `username`은 가입 규칙(필수·50자 이하), `new_password`는 가입 비밀번호 규칙(8~64자·UTF-8 72바이트 이하)을 따르고, 없으면 비밀번호를 바꾸지 않는다. 여러 단어 필드는 `@JsonProperty`로 snake_case(D-07).
  - 현재 비밀번호를 가장 먼저 확인한다. OWASP ASVS 4.0.3 V2.1.6은 비밀번호 변경에 현재·새 비밀번호를 함께 요구한다. 확인 전에 username 중복(409)을 알려 주지 않으므로, 탈취한 액세스 토큰만으로 가입된 이름을 떠볼 수 없다.
  - 틀리면 400 "현재 비밀번호가 일치하지 않습니다."로 응답한다. 401은 `WWW-Authenticate: Bearer`와 함께 토큰 문제를 뜻해, 클라이언트가 재발급을 시도하게 만들기 때문이다.
  - 비밀번호를 바꾸면 그 사용자의 리프레시 토큰을 모두 폐기한다(다른 기기의 세션 종료). 이미 발급된 액세스 토큰은 만료(최대 1시간)까지 유효하다(무상태 JWT — D-15와 같은 한계).
  - 구현: `UpdateUserRequest`·`UpdateUserCommand`, `UserUseCase.updateMe`, 도메인 `User.changeUsername`·`changePassword`, `InvalidCurrentPasswordException`(400 매핑), `UserService`에 `RefreshTokenCommandPort` 주입.
- **OPT-03 탈퇴** `DELETE /users/me`(인증, 본문 없음) → 204:
  - **사용자 행은 지우지 않고 탈퇴로 표시한다(soft delete).** SQLite PK에는 AUTOINCREMENT가 없다. 그래서 마지막 사용자를 지우면 다음 가입자가 같은 ID를 받는다(sqlite3 재현: id 3 삭제 → 새 가입자 id 3, 탈퇴 표시만 하면 새 가입자 id 4).
  - 액세스 토큰은 `sub`(사용자 ID)만 담고 만료까지 유효하다. 행을 지우면 탈퇴자에게 남은 토큰이 새 가입자의 계정에 닿는다(계정 탈취 경로).
  - Hibernate SQLite 방언은 PK를 표 끝의 `primary key (id)`로 만들기 때문에 `AUTOINCREMENT`를 붙일 수 없다(6.6.53 `SQLiteIdentityColumnSupport`·`StandardTableExporter` 소스).
  - 한 트랜잭션에서 처리한다: `withdrawn = true`, 그 사용자의 리프레시 토큰 전부 삭제, TODO 전부 삭제.
  - 탈퇴 뒤 응답: 로그인 401(같은 메시지), 재발급 401, 남은 액세스 토큰의 `GET`·`PUT`·`DELETE /users/me` 404. 탈퇴한 username은 다시 쓸 수 없다(가입 409). 행이 남아 유니크가 유지되며, 다른 사람이 탈퇴자의 이름으로 가입해 그 사람인 척하는 것도 막는다.
  - TODO 삭제는 기능 간 직접 참조 금지를 지키도록 이벤트로 넘긴다. 순서: `UserService` → `UserEventPort.publishWithdrawn(userId)` → `user.adapter.out.event.UserEventAdapter`가 Spring 이벤트 `global.event.UserWithdrawnEvent` 발행 → `todo.adapter.in.event.UserWithdrawnEventListener` → `TodoUseCase.deleteAll(userId)`.
  - 이벤트는 동기 `@EventListener`라 같은 트랜잭션·커넥션에서 실행된다. TODO 삭제가 실패하면 탈퇴 전체가 롤백된다. `@TransactionalEventListener(AFTER_COMMIT)`는 원자성이 깨져 쓰지 않는다.
  - 스키마: `users.withdrawn boolean default false not null`(`@ColumnDefault("false")`). 기본값이 없으면 기존 `todo.db`에 열을 더하는 `ddl-auto: update`가 SQLite에서 실패한다(sqlite3 확인: "Cannot add a NOT NULL column with default value NULL"). Hibernate 스키마 갱신은 기존 표에 `alter table … add column`을 시도한다(`StandardTableMigrator`).
  - 조회 포트: 로그인·내 정보는 탈퇴하지 않은 사용자만 찾는 `findActiveById`·`findActiveByUsername`(`…AndWithdrawnFalse`)로 바꾼다. `existsByUsername`은 탈퇴자를 포함한다.
  - 알려진 한계(수용): 탈퇴 전에 받은 액세스 토큰은 만료(최대 1시간)까지 TODO API를 호출할 수 있다. 그 ID는 다시 쓰이지 않으므로 다른 사용자에게 닿지 않는다. 막으려면 요청마다 사용자 상태를 조회해야 하고, 그러면 필수 API의 인증 경로가 바뀐다.
- **테스트**(이름에 `[OPT-0x]`):
  - 검색: 본인 것만·제목만·id 순, `%`·`_` 문자 그대로, 검색어 없음·공백 400, 토큰 없음 401.
  - 수정: 새 정보로 로그인 200, 옛 비밀번호 401, 비밀번호를 바꾸면 옛 refresh_token 401, 현재 비밀번호가 틀리면 400이고 아무것도 바뀌지 않음, username 중복 409.
  - 탈퇴: 204 → 로그인 401·재발급 401·`/users/me` 404, TODO 삭제(다른 사용자 TODO는 그대로), 같은 username 재가입 409. **새 가입자가 탈퇴자의 ID를 받지 않고, 탈퇴자 토큰으로 새 계정에 접근할 수 없음**을 확인한다.
- **대안**:
  - 하드 삭제 — AUTOINCREMENT를 쓸 수 없어 ID 재사용으로 계정 탈취 경로가 생긴다.
  - 요청마다 사용자 존재·토큰 발급 시각 확인 — 필수 API 인증 경로가 바뀌고 요청마다 조회한다.
  - 탈퇴 시 비밀번호 재확인 — DELETE 본문은 의미가 정의되지 않았다(RFC 9110 §9.3.5).
  - username 해제 — 등록할 수 없는 대체 값을 만들어야 하고 사칭 방지가 사라진다.
  - `UserService`가 todo 포트를 직접 호출 — 기능 간 참조 금지를 어긴다.
  - 탈퇴자 TODO를 남겨 둠 — 다른 사람에게는 닿지 않지만, 탈퇴한 사용자의 데이터가 남는다.
