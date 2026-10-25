---
paths:
  - "src/main/resources/**"
  - "src/test/resources/**"
  - "src/main/java/**/adapter/out/persistence/**"
---

# 영속성 규칙 — SQLite + Spring Data JPA (근거 D-10·D-15)

## 테이블 생성 경로 (제약)
- 테이블은 README에 적힌 명령(`./gradlew bootRun`, `./gradlew test`)을 실행하는 것만으로 생성된다. 생성 경로는 Hibernate `ddl-auto` 하나다(앱 `update`, 테스트 `create-drop`).
- 금지: 수동 SQL, `schema.sql`·`data.sql`(`spring.sql.init`), 별도 마이그레이션 명령·스크립트, 미리 만든 DB 파일 제공.
- README에는 SQL·`sqlite3` 명령을 적지 않는다(확인용으로도). 생성 확인은 개발 측 검증으로만 하고 근거를 `docs/progress.md`에 남긴다.

## 설정
```yaml
# src/main/resources/application.yaml (발췌)
spring:
  datasource:
    url: jdbc:sqlite:todo.db              # 실행 위치(프로젝트 루트) 기준, .gitignore·제출 제외
    hikari:
      maximum-pool-size: 1                # SQLite는 동시 writer 1개 → SQLITE_BUSY 예방 (실행으로 검증)
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update                    # 명세: README 명령만으로 테이블 생성
    open-in-view: false
```
- 테스트(`src/test/resources/application-test.yaml`): `url: jdbc:sqlite:build/todo-test.db`, `ddl-auto: create-drop`.
- SQLite는 상위 디렉터리를 만들지 않는다. 존재가 보장된 경로만 쓴다.
- 의존성과 버전은 `build.md`를 따른다.

## SQLite DDL 함정 (Hibernate 6.6.53 소스로 확인)
- **FK 제약은 생성되지 않는다**(`hasAlterTable()=false`라 FK DDL을 생략). `@ManyToOne`으로 무결성을 기대하지 말고 `user_id`(Long) + `@Index`로 두며, 무결성은 애플리케이션에서 보장한다.
- **유니크는 `@Column(unique = true)`로만 건다** — CREATE TABLE에 인라인으로 들어간다. `@Table(uniqueConstraints = ...)`는 ALTER가 필요해 SQLite에서 생략된다.
- PK: `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- 컬럼 타입은 Long·String·boolean만 쓴다. 시각은 epoch 밀리초 Long으로 저장한다(SQLite 날짜 타입 회피). Enum이 필요해지면 저장→조회 왕복 테스트로 먼저 확인한다.

## 테이블
| 테이블 | 컬럼 | 제약·인덱스 |
|---|---|---|
| `users` | `id`, `username`, `password` | `username` 유니크 |
| `todos` | `id`, `user_id`(필드명 `ownerId`), `title`, `description`, `completed` | `user_id` 인덱스 |
| `refresh_tokens` | `id`, `user_id`, `token_hash`, `expires_at`(epoch ms), `used` | `token_hash` 유니크, `user_id` 인덱스 |

## JPA 엔티티·리포지토리
- `adapter.out.persistence` 안에만 둔다. `protected` 기본 생성자, setter 없음, `from(domain)`/`toDomain()` 변환 메서드.
- 조회 메서드에는 소유자 조건을 넣는다: `findByIdAndOwnerId`, `findAllByOwnerIdOrderByIdAsc`. 사용자는 `findByUsername`, `existsByUsername`. 리프레시 토큰은 `findByTokenHash`, `deleteAllByUserId`, `deleteAllByUserIdAndExpiresAtLessThan`.
- 네이티브 쿼리(`@NativeQuery`, `nativeQuery = true`)와 요청값으로 만든 `Sort`·`Pageable`은 쓰지 않는다. 정렬은 메서드 이름에 고정한다(CVE-2026-47834 전제 회피).

## 검증
- `todo.db`를 지운 클린 상태에서 README 명령만 실행한 뒤 `sqlite3 todo.db '.schema'`로 세 테이블·`unique`·인덱스 생성을 확인한다.
- DB 설정을 바꾸면 기동과 `./gradlew test`를 다시 실행한다.
