---
paths:
  - "src/main/resources/**"
  - "src/test/resources/**"
  - "src/main/java/**/adapter/out/persistence/**"
---

# 영속성 규칙 — SQLite + Spring Data JPA (근거 D-10·D-15)

## 테이블 생성 경로 (제약)
- 테이블은 README에 적힌 명령(`./gradlew bootRun`, `./gradlew test`)을 실행하는 것만으로 생성된다. 생성 경로는 Hibernate `ddl-auto` 하나다(앱 `update`, 테스트 `create-drop`).
- `ddl-auto`는 애플리케이션이 기동될 때(Tomcat이 요청을 받기 전) 실행되므로 실행 방법(README 명령·IDE·`java -jar`)과 상관없이 테이블이 생기고, 재기동은 기존 테이블·데이터를 그대로 둔다. DB 파일은 프로세스의 작업 디렉터리에 생긴다(`bootRun`은 프로젝트 루트) — S11 실측.
- 금지: 수동 SQL, `schema.sql`·`data.sql`(`spring.sql.init`), 별도 마이그레이션 명령·스크립트, 미리 만든 DB 파일 제공.
- README에는 SQL·`sqlite3` 명령을 적지 않는다(확인용으로도). 생성 확인은 개발 측 검증으로만 하고 근거를 `docs/progress.md`에 남긴다.

## 설정 — 기준은 `application.yaml`·`application-test.yaml`이며 아래 값은 바꾸지 않는다
- dialect `org.hibernate.community.dialect.SQLiteDialect`, `ddl-auto` 앱 `update`·테스트 `create-drop`, `open-in-view: false`.
- Hikari `maximum-pool-size: 1` — SQLite는 동시 writer가 1개라 SQLITE_BUSY를 막는다(요청 처리 중 동작은 7단계 이후 확인, R-3).
- DB 파일: 앱 `todo.db`(실행 위치 = 프로젝트 루트, `.gitignore`·제출 제외), 테스트 `build/todo-test.db`. SQLite는 상위 디렉터리를 만들지 않으므로 존재가 보장된 경로만 쓴다.
- 의존성과 버전은 `build.md`를 따른다.

## SQLite DDL 함정 (Hibernate 6.6.53 소스로 확인)
- **FK 제약은 생성되지 않는다**(`hasAlterTable()=false`라 FK DDL을 생략). `@ManyToOne`으로 무결성을 기대하지 말고 사용자 ID 컬럼(`user_id`, Long) + `@Index`로 두며, 무결성은 애플리케이션에서 보장한다.
- **유니크는 `@Column(unique = true)`로만 건다** — CREATE TABLE에 인라인으로 들어간다. `@Table(uniqueConstraints = ...)`는 ALTER가 필요해 SQLite에서 생략된다.
- PK: `@GeneratedValue(strategy = GenerationType.IDENTITY)`. SQLite 방언은 이를 AUTOINCREMENT 없는 `integer` PK(rowid 별칭)로 만든다(`SQLiteIdentityColumnSupport`) → **마지막 행을 지우면 그 ID가 다음 행에 다시 쓰인다**(다른 사용자의 새 TODO일 수도 있다 — sqlite3로 재현). 소유권 조건(`id` + `userId`)이 있어 타인 데이터는 드러나지 않으며, 코드·테스트는 삭제한 ID가 계속 비어 있다고 가정하지 않는다.
- 인덱스는 `@Table(indexes = @Index(columnList = "userId"))`처럼 필드명으로 적는다 — Hibernate가 컬럼과 같은 명명 전략을 적용한다(`IndexBinder`). `CREATE INDEX`는 ALTER가 아니라 SQLite에서도 생성된다.
- 컬럼 타입은 Long·String·boolean만 쓴다. 시각은 epoch 밀리초 Long으로 저장한다(SQLite 날짜 타입 회피). Enum이 필요해지면 저장→조회 왕복 테스트로 먼저 확인한다.

## 테이블
| 테이블 | 컬럼 | 제약·인덱스 |
|---|---|---|
| `users` | `id`, `username`, `password` | `username` 유니크 |
| `todos` | `id`, `user_id`, `title`, `description`, `completed` | `user_id` 인덱스 |
| `refresh_tokens` | `id`, `user_id`, `token_hash`, `expires_at`(epoch ms), `used` | `token_hash` 유니크, `user_id` 인덱스 |

## JPA 엔티티·리포지토리
- `adapter.out.persistence` 안에만 둔다. `protected` 기본 생성자, setter 없음, `from(domain)`/`toDomain()` 변환 메서드.
- 이름(D-20): 엔티티 `<대상>JpaEntity`, Spring Data 인터페이스 `<대상>Repository`, 이를 주입해 `<대상>CommandPort`·`<대상>QueryPort`를 함께 구현하는 `<대상>RepositoryAdapter`.
- 조회 메서드에는 소유자 조건을 넣는다: `findByIdAndUserId`, `findAllByUserIdOrderByIdAsc`. 사용자 ID 필드는 두 테이블 모두 `userId`(컬럼 `user_id`)로 쓰며, Boot 기본 명명 전략이 바꿔 주므로 `@Column(name)`을 붙이지 않는다. 사용자는 `findByUsername`, `existsByUsername`. 리프레시 토큰은 `findByTokenHash`, `deleteAllByUserId`, `deleteAllByUserIdAndExpiresAtLessThan`.
- 네이티브 쿼리(`@NativeQuery`, `nativeQuery = true`)와 요청값으로 만든 `Sort`·`Pageable`은 쓰지 않는다. 정렬은 메서드 이름에 고정한다(CVE-2026-47834 전제 회피).

## 검증
- `todo.db`를 지운 클린 상태에서 `./gradlew bootRun`(README 작성 뒤에는 README 명령)만 실행하고 `sqlite3 todo.db '.schema'`로 테이블·`unique`·인덱스 생성을 확인한다 — 7단계 `users`·`todos`, 8단계 `refresh_tokens`, README 단계에서 최종 확인(TECH-06).
- DB 설정을 바꾸면 기동과 `./gradlew test`를 다시 실행한다.
