# 설계 결정 기록 (ADR-lite)

> 이후 작업에 영향을 주는 결정만 기록한다. 결정은 삭제하지 않는다.
> 상태: **확정** · **확정(실측 조건)** — 구현 단계 검증 결과에 따라 재검토 · **대체됨(→D-xx)**
> 형식: 결정 / 근거(검증 출처) / 대안. 명세 원문 사본은 `docs/assignment-spec.md`(로컬 전용).

| ID | 결정 | 상태 |
|---|---|---|
| D-01 | 빌드 기준선: Boot 3.5.16 · Gradle 9.8.2 · Java 17 타깃 · Tomcat 10.1.61 | 확정(실측 조건) |
| D-02 | Lombok·springdoc 유지와 사용 범위 | 확정 |
| D-03 | 기능별 헥사고날 + 경량 DDD, 포트 정책 | 확정 |
| D-04 | 도메인 모델·JPA 엔티티 분리, 집합체 간 ID 참조 | 확정 |
| D-05 | 액세스 토큰: OAuth2 Resource Server(Nimbus) + HS256, clock skew 0 | 확정 |
| D-06 | 서명 키(미설정 시 임의 키)·액세스 토큰 수명 1시간 | 확정 |
| D-07 | 응답 규약: 토큰 응답 필드 명시 · ProblemDetail · 상태 코드 | 확정 |
| D-08 | 소유권 검사와 존재 은닉(타인 TODO 404) | 확정 |
| D-09 | 비밀번호·로그인 정책(BCrypt 72바이트 제한 대응) | 확정 |
| D-10 | SQLite 영속성: 테이블 생성 경로 단일화, DDL 함정 대응, 커넥션 풀 1 | 확정(실측 조건) |
| D-11 | 테스트 전략: 통합 테스트 + 실제 로그인 토큰 | 확정 |
| D-12 | 요청·응답 필드 | 확정 |
| D-13 | 작업 관리 체계, 단계 게이트, 공개 저장소 운영 | 확정 |
| D-14 | 선택 사항(가산점) 처리 정책 | 확정 |
| D-15 | 토큰 재발급(Refresh Token) 확장과 필수 테스트 보호 | 확정 |
| D-16 | 프롬프트 기록 보존(같은 턴 기록·훅 자동 백업·백필) | 확정 |

---

### D-01 빌드 기준선 — 확정(실측 조건)
- **결정**: Spring Boot **3.5.16**, Gradle Wrapper **9.8.2 유지**, Java **17 타깃**(`options.release = 17`, toolchain 없음), Tomcat **10.1.61** 오버라이드. (사용자 결정 2026-10-25)
- **근거**:
  - 명세 조건은 Java 17 이상·JUnit 5·Gradle/Maven이며 Spring Boot 버전 조건은 없다("3.x"는 초안 조건). Boot 4.1.2 BOM은 JUnit Jupiter 6.0.3, Boot 3.5.16 BOM은 5.12.2를 관리한다 → 3.5.16.
  - Gradle 공식 호환표: Gradle 8.14는 Java 24까지 실행, Java 25는 9.1.0 이상, 26은 9.4.0 이상 필요 → 채점 JDK를 모르므로 9.8.2 유지. Boot 3.5 System Requirements는 Gradle 7.6.4+/8.4+만 명시하므로 기준선 단계에서 실측한다.
  - Gradle toolchain은 같은 버전 JDK가 있어야 동작 → `release 17`로 JDK 17 이상 어디서나 빌드. 이 과제에 Java 21 전용 기능은 필요 없다.
  - 보안: 3.5 계열 OSS 지원은 2026-06-30 종료. 이후 공개된 CVE-2026-59270(임베디드 UnboundID LDAP)·CVE-2026-47842(`AesBytesEncryptor`)·CVE-2026-47886(신뢰할 수 없는 SpEL 평가)·CVE-2026-47834(Spring Data JPA 3.5.13, 네이티브 쿼리+요청 Sort)은 3.x/6.x 계열 수정이 엔터프라이즈 전용이지만, 전제 기능을 쓰지 않는다(spring.io 권고의 조건 확인) → `security.md` 사용 금지 기능으로 고정. Tomcat 10.1.56~10.1.61에는 Apache의 OSS 보안 수정이 있으므로 버전만 올린다.
- **대안**: Boot 3.5.16 + Gradle 8.14.6(공식 조합, JDK 25 이상에서 실행 불가) · Boot 4.1.2(지속 패치, JUnit 6이라 명세와 불일치) · Java 21 toolchain(채점 환경에 JDK 21 필요).
- **실측 계획**: JDK 17·21로 `./gradlew clean test/build --warning-mode all`, class major 61, `dependencyInsight tomcat-embed-core`. 실패하면 8.14.6으로 폴백하고 README에 "JDK 17~24"를 명시한다.
- **실측 결과(3단계, 2026-10-25)**: Gradle 9.8.2 + Boot 3.5.16으로 JDK 21·17 모두 `clean compileJava compileTestJava assemble`(bootJar 포함) 성공, class major 61. 해석된 버전 — spring-boot 3.5.16, spring-core 6.2.19, spring-security-oauth2-jose 6.5.11, hibernate-core·community-dialects 6.6.53.Final, sqlite-jdbc 3.49.1.0, tomcat-embed-core 10.1.61(rule로 선택), springdoc 2.8.17, junit-jupiter 5.12.2. Boot 플러그인(`JavaPluginAction`)이 `Configuration.setVisible`을 써서 deprecation 경고 1건(Gradle 11 제거 예정 — 9.x 영향 없음). 테스트 실행 검증은 4단계에서 한다.
- **검증 정책(사용자 결정 2026-10-25)**: 기본 개발·검증은 JDK 17(타깃·명세 최소 버전), 단계 완료와 제출 전에 JDK 21로 한 번 교차 확인한다. 21 확인의 목적은 코드가 아니라 빌드 환경이다 — toolchain이 없으므로 채점자의 JDK에서 Gradle·javac·Lombok 애너테이션 프로세서·테스트 라이브러리가 실행되며, 대상이 17이어도 새 JDK에서 빌드가 깨질 수 있다(예: Lombok 1.18.28 이하는 JDK 21 javac에서 컴파일 실패, Gradle 8.14는 JDK 25에서 실행 불가).

### D-02 Lombok·springdoc 유지 — 확정
- **결정**: 둘 다 유지(사용자 결정). Lombok은 main 코드에만 쓰고 사용 범위를 제한한다(architecture.md). springdoc은 Boot 3.5용 2.8.17. Swagger 경로는 `permitAll`, `@SecurityRequirement`는 보호 API에만 붙인다.
- **근거**: 채점자가 브라우저로 API를 시험할 수 있다. springdoc 2.8.17 POM은 Boot 3.5.13 기준이다. springdoc 2.8.17의 `ObjectMapperProvider`는 자체 `Json.mapper()`를 써서 Spring의 전역 Jackson 네이밍 전략을 스키마에 반영하지 않는다(소스 확인) → D-07.
- **대안**: springdoc 제거(YAGNI 관점의 권장안이었음).

### D-03 기능별 헥사고날 + 경량 DDD — 확정
- **결정**: `user`, `todo` 패키지마다 `domain` / `application`(`port.in`, `port.out`, `service`) / `adapter`(`in.web`, `out.persistence`, `out.security`), 공통은 `global`(`security`, `error`, `config`). In Port는 책임별 인터페이스(`UserUseCase` 가입·내 정보, `AuthUseCase` 로그인·재발급, `TodoUseCase`), Out Port는 `UserRepository`·`TodoRepository`·`RefreshTokenRepository`·`PasswordHasher`·`TokenIssuer`. Command record는 파라미터가 많은 유스케이스에만 쓴다.
- **근거**: 초안 §5 "저장소 및 외부 기능은 Port를 통해 접근", "도메인은 기술 세부사항에 직접 의존하지 않는다". 평가 기준 "코드를 읽기 쉬운가"와 초안의 "의미 없는 인터페이스 금지"에 따라 유스케이스별 인터페이스(9개) 대신 책임별 3개로 묶었다. 재발급 확장(D-15)으로 인증 책임이 커져 `AuthUseCase`를 계정 관리(`UserUseCase`)와 나눴다. 리프레시 토큰 생성·해시는 JDK 표준 API로 충분해 포트를 만들지 않는다.
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

### D-07 응답 규약 — 확정
- **결정**: 로그인·재발급 응답은 `{"access_token": ..., "refresh_token": ...}`이고 두 필드에 `@JsonProperty`로 snake_case를 명시한다(사용자 결정 2026-10-25). 다른 필드는 모두 한 단어이며, 여러 단어 필드를 추가하면 같은 방식으로 명시한다. 오류는 RFC 9457 ProblemDetail(401 포함), 상태 코드는 `.claude/rules/api.md` 표(201+Location·200·204·400·401·404·409)를 따른다.
- **근거**: 명세의 `access_token` 표기를 그대로 지키고 확장 필드만 더한다(RFC 6749 §5.1 토큰 응답 관례). 전역 `SNAKE_CASE`는 springdoc 스키마에 반영되지 않아(D-02) 필드 단위 명시가 단순하고 정확하다. 초안 §3.5 "명세 외 응답은 프로젝트 전체에서 일관". ProblemDetail은 Spring Framework 6 내장이라 추가 의존성이 없다.
- **대안**: 전역 SNAKE_CASE + springdoc용 `ModelResolver` 빈 — 설정이 두 곳으로 나뉜다. 리프레시 토큰을 HttpOnly 쿠키로 전달 — D-15 대안 참조.

### D-08 소유권 검사와 존재 은닉 — 확정
- **결정**: 단건 조회·수정·삭제는 `id + ownerId` 조건으로 조회하고, 미존재·타인 소유 모두 404로 응답한다.
- **근거**: 명세 "로그인한 사용자 본인의 TODO만 다룹니다". RFC 9110 §15.5.4: 대상의 존재를 숨기려는 서버는 403 대신 404로 응답할 수 있다. IDOR 방지.
- **대안**: 403 — 다른 사용자의 리소스가 존재한다는 사실이 드러난다.

### D-09 비밀번호·로그인 정책 — 확정
- **결정**: `DelegatingPasswordEncoder`(bcrypt). 비밀번호 8~64자 + UTF-8 72바이트 이하, username 중복은 409, 로그인 실패는 하나의 401 메시지.
- **근거**: Spring Security 6.5.11 `BCrypt.java:615-616`이 72바이트 초과 시 `IllegalArgumentException("password cannot be more than 72 bytes")`을 던진다 → 검증하지 않으면 500(CVE-2025-22228 대응 이후 동작). 회원가입 409가 이미 계정 존재를 드러내므로 로그인 응답 시간 균등화(더미 해시 비교)는 하지 않는다(YAGNI). 메시지 통일은 비용이 없어 유지한다.

### D-10 SQLite 영속성 — 확정(실측 조건)
- **결정**: 테이블 생성 경로는 README 명령(`./gradlew bootRun`, `./gradlew test`)이 일으키는 Hibernate `ddl-auto` 하나다(앱 `update`, 테스트 `create-drop`). 수동 SQL·`schema.sql`·`data.sql`·마이그레이션 명령·미리 만든 DB 파일과 README의 SQL 명령을 금지한다(사용자 요청 2026-10-25). dialect 명시, `open-in-view: false`, Hikari `maximum-pool-size: 1`, DB 파일은 앱 `./todo.db`·테스트 `build/todo-test.db`. FK 대신 `user_id` 인덱스 + 애플리케이션 무결성, 유니크는 `@Column(unique = true)`. 컬럼 타입은 Long·String·boolean이고 시각은 epoch 밀리초 Long. 네이티브 쿼리·요청 Sort는 쓰지 않는다.
- **근거**: 명세 "README에 적힌 명령어만으로 테이블이 생성되어야 합니다. (지원자가 SQL을 직접 실행해야 하는 방식은 안 됩니다)". 생성 경로를 하나로 두면 채점 환경에서 결과가 결정적이다. Boot 3.5.16 BOM — sqlite-jdbc 3.49.1.0, hibernate-community-dialects = Hibernate 6.6.53.Final. 소스로 확인 — SQLiteDialect `hasAlterTable()=false` → `StandardForeignKeyExporter`가 FK DDL을 만들지 않는다. SQLite용 유니크 처리는 컬럼 정의에 `unique`를 인라인으로 붙이고, ALTER가 필요한 테이블 단위 유니크 제약은 생략한다. `busy_timeout` 기본 3000ms, SQLite는 동시 writer 1개.
- **위험**: 풀 1개에서 연결 대기 타임아웃이 생기면 기본 풀 + busy_timeout 방식으로 재검토한다.

### D-11 테스트 전략 — 확정
- **결정**: `@SpringBootTest` + MockMvc + `test` 프로파일. 보안 필터를 포함하고 실제 `/users/login` 토큰을 쓴다. 고유 username으로 격리한다. `spring-security-test`는 넣지 않는다.
- **근거**: 필수 시나리오가 HTTP 흐름(가입→로그인→조회 등)이고 명세는 단위·통합 방식을 자유로 둔다. 통합 테스트가 핵심 흐름의 직접적인 증거이며, 실제 토큰을 쓰므로 목 인증 라이브러리가 필요 없다.

### D-12 요청·응답 필드 — 확정
- **결정**: 가입·로그인 `username`, `password` / 내 정보 `{id, username}` / 재발급 요청 `refresh_token` / TODO `title`(필수), `description`(선택), `completed`(기본 false) / 목록은 배열·id 오름차순 / PUT은 전체 교체.
- **근거**: 명세 원문(2026-10-25 확인)에 필드 정의가 없어 최소 필드로 정했다. 명세 선택 기능의 "제목으로 검색"과 `title`이 맞는다.

### D-13 작업 관리 체계, 단계 게이트, 공개 저장소 운영 — 확정
- **결정**: `CLAUDE.md`(상시 로드) + `.claude/rules/` 7개(경로 한정 자동 로드) + `.claude/skills/submission-check`(수동 실행 절차) + `docs/`(체크리스트·결정·진행, 명세 사본) + `prompts/used_prompts.md`(명세대로 "간단히"). 진행은 설계 승인 → 구현 단계별 계획·구현·검증·보고 → 사용자 확인 후 다음 단계. 원격 저장소는 `https://github.com/manners-parkpro/jwt-todo-assignment.git`(공개, `main`)이며, 명세 사본 `docs/assignment-spec.md`는 `.gitignore`로 공개 저장소에서 제외하고 로컬·제출 ZIP에만 둔다(사용자 결정). 커밋·푸시는 사용자 요청 시에만 한다. 커밋 작성자는 저장소 로컬 git 설정으로 닉네임 `Colin`과 GitHub noreply 주소(`43167000+manners-parkpro@users.noreply.github.com`)를 쓴다 — 회사 도메인 제외(사용자 요청 2026-10-25). 이미 올린 초기 커밋은 작성자 정보만 바꿔 다시 만들었고(트리 동일), 사용자가 원격 저장소를 삭제·재생성한 뒤 다시 푸시했다(이전 커밋은 조회 불가 — HTTP 409). 푸시 인증은 저장소 로컬 git 설정의 자격 증명 헬퍼가 `gh auth token --user manners-parkpro`를 쓰게 한다 — gh의 기본 헬퍼는 활성 계정 토큰만 반환하므로(gh 2.90.0 소스 확인), 전역 계정 전환 없이 이 저장소만 개인 계정으로 푸시하고 다른 저장소 설정과 gh 활성 계정은 그대로 둔다(사용자 결정). 비교 대상이던 전역 전환(`gh auth switch`)은 전환 중 다른 저장소에 영향을 주고, 다른 계정에 권한을 주는 방식은 푸시 계정이 저장소 활동 기록에 남아 채택하지 않았다.
- **근거**: Claude Code 공식 문서(memory·best-practices·skills) — CLAUDE.md는 200줄 미만 권장, 일부 영역 규칙은 path-scoped rule(Read/Write/Edit 시 로드), 다단계 절차는 skill, `@import`는 컨텍스트를 줄이지 못함, 자명한 지침은 제외. Anthropic "Effective harnesses for long-running agents" — 진행 로그·기능 목록·git, 세션 시작 시 기준선 확인, 한 번에 한 기능. 회사 과제 명세의 공개 재배포는 피한다.
- **대안**: 단일 CLAUDE.md(초안 458줄) — 권장 크기를 넘어 지침 준수율이 떨어진다.

### D-14 선택 사항(가산점) 처리 — 확정
- **결정**: OPT-01~03(검색, 회원 수정·탈퇴)은 필수 항목(TEST-01~08 통과, README 실검증)을 마친 뒤 범위와 위험을 다시 판단해 결정한다. 추가 테스트(TEST-X)는 필수 4종 완료 후 작성한다.
- **근거**: 명세 "안 해도 감점 없음", 평가 1순위는 요구사항대로 동작하는가, YAGNI. 회원 수정(비밀번호 변경)과 탈퇴는 리프레시 토큰 폐기와 TODO 정리(FK 없음) 같은 추가 결정이 필요하다.

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

### D-16 프롬프트 기록 보존 — 확정
- **결정**:
  - `prompts/used_prompts.md`(제출용 요약 기록)는 프롬프트를 받은 턴 안에 갱신하고 커밋한다.
  - `UserPromptSubmit` 훅(`.claude/hooks/log-prompt.py`, 설정은 로컬 `.claude/settings.local.json`)이 제출되는 모든 프롬프트 원문을 `.claude/prompt-history.jsonl`(권한 600, 공개 저장소·ZIP 제외)에 자동으로 덧붙인다.
  - 이번 세션 프롬프트 7건은 `~/.claude/history.jsonl`과 붙여넣기 캐시에서 원문을 복원해 같은 파일에 백필했다.
  - 새 세션은 시작할 때 백업 파일과 `used_prompts.md`를 대조해 빠진 기록을 먼저 채운다(CLAUDE.md 세션 루틴).
- **근거(검증)**:
  - 컨텍스트 초기화·압축은 디스크 파일을 지우지 않지만, 대화 속 원문은 압축 때 요약되고 초기화 후에는 다음 세션의 맥락에 없다(Claude Code 문서 "What survives compaction": 프로젝트 루트 CLAUDE.md는 디스크에서 다시 주입, 대화는 요약).
  - 트랜스크립트(`~/.claude/projects/…/*.jsonl`)와 `paste-cache/`는 `cleanupPeriodDays`(기본 30일)가 지나면 자동 삭제된다. `history.jsonl`은 남지만 큰 붙여넣기는 해시 참조만 저장한다 — 실측 결과 첫 프롬프트의 초안 원문(18.7KB)은 `paste-cache`에만 있었다.
  - 훅은 프롬프트 제출마다 실행되며 입력의 `prompt` 필드로 원문을 받는다. stdout은 컨텍스트에 추가되고 종료 코드 2는 프롬프트를 막으므로, 출력 없이 기록하고 실패하면 1로 끝낸다(경고만 표시). 설정 파일 변경은 파일 감시자가 자동으로 반영한다.
- **검증**: 파이프 테스트(정상: 종료 0·stdout 없음·권한 600·원문 일치·덧붙이기 / 실패: 종료 1), jq 스키마 검증, 셸 형식 명령 실행, 백필 7건 중 복원 실패 0건. 실제 훅 실행은 다음 프롬프트에서 확인한다.
- **대안**: `cleanupPeriodDays` 상향(전역 설정이라 모든 프로젝트에 영향, 붙여넣기 캐시만으로 남는 문제는 그대로) · 원문 로그를 공개 저장소에 커밋(프롬프트 원문 전체 공개).
