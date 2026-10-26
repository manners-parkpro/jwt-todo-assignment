---
paths:
  - "src/test/**"
---

# 테스트 규칙 (근거 D-06·D-11)

## 우선순위
1. 필수 4종 TEST-01~04(단계는 `docs/requirements-checklist.md`)를 먼저 완성하고 통과시킨다.
2. 사용자 요청 확장(재발급) 테스트 TEST-07·08은 재발급 기능 구현 단계에서 함께 작성한다.
3. 그다음 선택 테스트 TEST-X1~X4(위조·만료 토큰 401, 타인 TODO 404, 중복 가입 409, 입력 검증 400). 커버리지만을 위한 테스트는 만들지 않는다.
   - 예외: TEST-X2는 명세의 필수 동작("로그인한 사용자 본인의 TODO만", API-09)을 검증하므로 TODO 기능 단계(7단계)에서 TEST-02·04 다음에 작성한다.

## 필수 테스트 보호 (D-15)
- TEST-01~04는 확장 기능(Refresh Token, 선택 사항)과 독립적으로 작성한다. TEST-01은 로그인 응답에서 `access_token`만 읽고 `refresh_token`이 있다고 가정하지 않는다.
- 확장 기능을 구현한 뒤에는 TEST-01~04를 다시 실행해 회귀가 없는지 확인한다. 확장 때문에 필수 테스트를 수정·약화·생략하지 않는다.

## 구성
- 통합 테스트: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`를 공통 베이스 `IntegrationTestSupport`에 둔다.
- 보안 필터를 포함한 실제 흐름으로 검증한다. 토큰은 `/users/login` 응답에서 추출한다(`@WithMockUser`·목 인증 미사용). 준비 단계의 가입·로그인은 `IntegrationTestSupport`의 도우미로 공유한다(TEST-01은 그 흐름이 시나리오 자체라 직접 작성한다).
- JWT 키: 테스트는 `JWT_SECRET` 없이 실행되어 애플리케이션이 기동 시 만든 임의 키를 쓴다. 저장소에 테스트 키를 두지 않는다.
- 만료 토큰은 컨텍스트의 `JwtEncoder`로 `exp`가 현재보다 과거인 토큰을 발급한다(clock skew 0). 위조 토큰은 다른 임의 키로 서명한다.
- 테스트 이름에 체크리스트 ID를 넣는다: `@DisplayName("[TEST-01] 회원가입 → 로그인 → 내 정보 조회")`.

## 격리·반복 실행
- 테스트 DB는 `build/todo-test.db`, `ddl-auto: create-drop`이다(개발 DB `todo.db`와 분리).
- 테스트마다 고유 username(UUID 접미사)을 쓰고 다른 테스트의 데이터나 실행 순서에 의존하지 않는다.
- "존재하지 않는 ID"는 한 번도 발급되지 않는 값(`Long.MAX_VALUE`)을 쓴다. SQLite는 마지막 행을 지우면 그 ID를 새 행에 다시 쓰므로(`persistence.md`) 삭제한 ID가 계속 없다고 가정하지 않는다.
- 같은 명령을 두 번 연속 실행해도 결과가 같아야 한다. 실행 JDK는 `build.md` 검증 명령을 따른다.

## 검증 원칙
- 상태 코드와 핵심 본문 필드(jsonPath)를 함께 검증한다. 예: TEST-02 삭제 후 단건 조회 404와 목록 미포함을 모두 확인한다. TEST-04는 명세 문구("존재하지 않는 TODO ID로 요청하면")대로 `{id}`를 받는 GET·PUT·DELETE를 모두 확인한다.
- 기대값을 약화·삭제하거나 `@Disabled`로 실패를 숨기지 않는다. 명세와 테스트가 충돌하면 명세를 따르고 `docs/decisions.md`에 기록한다.
- 결과는 실제 출력(통과·실패 수, 실패 메시지)으로 보고한다. 상세 결과: `build/reports/tests/test/index.html`.
