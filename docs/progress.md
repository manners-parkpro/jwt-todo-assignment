# 진행 상황

> "현재 상태"와 "다음 작업"은 항상 최신으로 유지한다. 세션 로그는 맨 위에 추가하고, 약 150줄을 넘으면 오래된 로그를 요약한다.

## 현재 상태 — 2026-10-27
- **단계: 13단계 완료 — 제출 ZIP 생성·검증 완료(S18). 남은 일은 사용자가 채용 사이트에 업로드하는 것뿐이다.** 제출 ZIP은 커밋 `bc778a7`(CI run 40475001968 성공)에서 SKILL 명령으로 만든 프로젝트 상위 폴더의 `<이름>_springboot.zip`이다(`.git/` 제외·최상위 폴더 `assignment/` 포함 — 사용자 결정). 그 뒤 커밋은 이 기록 갱신뿐이다. 사용자 지시(#14)로 11단계를 10단계보다 먼저 했다. 설계 승인은 2026-10-25(used_prompts #2).
- README(S17·S18): 명세 4항목(스택·실행·테스트·API 목록)과 인증·검증 규칙·오류 응답·Swagger UI·curl 예시·구조. Gradle 명령은 CI로 3개 OS × JDK 17·21(Windows는 기본 셸 PowerShell 5.1), curl 예시는 Linux·macOS(bash·zsh)에서 적힌 그대로 실행해 확인한다. S18에 검증 규칙 표를 코드와 일치시켰다(리뷰 반영, 실서버 18건 실측).
- 빌드·테스트: JDK 17·21 `clean build` 45건 통과(S18 — 필수 TEST-01~04, 확장 07·08·09, 선택 X1·X2·X3·X4, API-10, OPT-01~03). 시험 ZIP 2개를 풀어 README 명령 그대로 전체 실측(S18, 두 번째는 한글·공백 경로). CI — `1c271a6`(run 40442195752)·`b50d0b2`(run 40442593113)·`bc778a7`(run 40475001968, 제출 ZIP의 커밋) 6개 조합 성공: Windows(PowerShell 5.1)·Linux·macOS × JDK 17·21 테스트 실행, `bootRun` 스모크 15회, 8080 사용 중 다른 포트 스모크 15회, README curl 예시(Linux·macOS) 상태 16개 일치, 경고·오류 0.
- 사용자 결정(S12·S13): Lombok 1.18.48(JDK 27 실격 위험 해결), 정기 교차 확인 JDK는 17·21, `memory-bank/`는 로컬에 두고 Git·ZIP에서만 제외, TTL은 yml 유지, Windows·Linux는 GitHub Actions로 실측. 서명 키 환경 변수는 `TODO_JWT_SECRET`(S13 — 흔한 이름 충돌로 기동 실패하는 위험 보강).
- 코드 구조(S10, D-20): 유스케이스는 Result를 반환하고 웹 어댑터가 Response로 변환한다. 포트·어댑터 이름은 사용자가 지정한 참고 프로젝트 규칙(CommandPort·QueryPort, RepositoryAdapter)을 따른다.
- 원격: 저장소 로컬 자격 증명 헬퍼로 푸시한다. 원격 커밋 작성자·커미터는 모두 noreply다.
- 지침·기록(S7): 압축 뒤 CLAUDE.md 전체 주입 확인. 로컬 훅 3개 — 프롬프트 원문 백업(하네스 메시지 제외), 지침 로드 기록, 커밋·푸시 검사(작성자·테스트·강제 푸시).

## 다음 작업 (한 단계씩 — 계획·구현·검증·독립 리뷰·보고 후 사용자 확인)
1. [x] 사용자 확인 사항 처리 — 명세 확보, D-01·D-02 결정, `memory-bank/` 삭제(백업), `git init`
2. [x] 저장소 정리·원격 연결 — `.gitignore` 보강, Initializr 잔여물 삭제, origin 연결
3. [x] 빌드 기준선 교정 — `build.gradle` Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17·Lombok main만 (검증: S4)
4. [x] 설정과 테스트 베이스 — `application.yaml`(SQLite·JPA), `application-test.yaml`, `IntegrationTestSupport` (검증: S6). `auth.*`는 사용하는 코드와 함께 5단계로 조정
5. [x] 보안 골격: `auth.jwt-secret`·SecurityConfig·서명 키와 JWT 디코더(임의 키 대체)·공개 인증 경로용 `BearerTokenResolver`·EntryPoint·GlobalExceptionHandler → TEST-03 — SEC-02·04·07·08·12, ERR-01 (검증: S8)
6. [x] User·Auth 필수 범위: `JwtEncoder`(HS256 헤더 명시)·`TokenIssuer`·비밀번호 해시 어댑터·`@CurrentUserId`, 가입·내 정보·로그인(**`access_token`만 발급**) → TEST-01 — API-01~03, SEC-01·03·06 (검증: S9)
7. [x] Todo 기능 → TEST-02·04(GET·PUT·DELETE) + TEST-X2(본인 TODO만 — 명세 필수 동작이라 S10에 10단계에서 앞당김), 클린 상태 `bootRun`으로 `users`·`todos` `.schema` 중간 확인, 동시 요청으로 커넥션 풀 1개 동작 확인(R-3) — API-04~09, ERR-02·03, DB-02·03. 사용자 지시로 Result → Response 변환·네이밍 정리(D-20)를 user 기능까지 적용 (검증: S10)
8. [x] 재발급 확장: 로그인 응답에 `refresh_token` 추가, `refresh_tokens`, `POST /users/refresh`(회전·재사용 탐지, 공개 엔드포인트 매처에 추가) → TEST-07·08 + TEST-09(만료, S12 추가) + **TEST-01~04 회귀**, `refresh_tokens` `.schema` 확인 — API-11, SEC-11, DB-06 (검증: S12)
9. [x] springdoc 설정(보안 스키마, 보호 API 표시) → `/v3/api-docs` 확인 — API-10 (검증: S14)
10. [x] 선택 테스트 TEST-X1·X3·X4(X2는 7단계), 2회 연속 실행, `./gradlew build` — SEC-05, TEST-06, ERR-04 (검증: S16, 11단계 뒤에 진행)
11. [x] 선택 기능 OPT-01~03 구현 — D-21, OPT-01 → 02 → 03 순서로 기능마다 `[OPT-0x]` 테스트·TEST-01~04 회귀·`ApiDocsTest` 목록 갱신 (검증: S15, 사용자 지시로 10단계보다 먼저)
12. [x] README 작성과 명령 실검증(최종 API 기준, macOS/Linux 실측 + Windows 표준 Wrapper 명령 — CI로 3개 OS × JDK 17·21 확인, SQL 명령 없음, 8080 포트 사용 중일 때의 실행 방법 실측), 클린 상태에서 README 명령으로 테이블 생성 최종 확인 — DOC-01~05, TECH-06·07, D-18 (검증: S17)
13. [x] `/submission-check <이름>` — SUB-01~06, SEC-09·10, DOC-06~08(리뷰 지적으로 SEC-03·07·TEST-05·D-17 서술도 정리) (검증: S18 — 제출 ZIP 생성·검증 완료, 업로드는 사용자)

## 환경 메모
- JDK: 이 PC 기본은 Corretto 21.0.10. 기본 검증은 OpenJDK 17.0.18이며, 셸 환경 변수가 Bash 호출마다 초기화되므로 명령마다 `JAVA_HOME=$(/usr/libexec/java_home -v 17)`을 붙인다. `java -version`은 PATH 앞쪽의 Corretto 21을 가리키므로 사용 JDK는 `./gradlew --version`의 Launcher JVM으로 확인한다. 단계 완료 시 21로 교차 확인. 전역 Gradle 없음 → `./gradlew`만 사용
- sqlite3 3.51.0(macOS 기본), openssl·zip·unzip·jq 사용 가능. macOS에는 `timeout` 명령이 없다
- Git: 브랜치 `main`, 원격 `origin` = https://github.com/manners-parkpro/jwt-todo-assignment.git (공개). 저장소 로컬 작성자 = `Colin <43167000+manners-parkpro@users.noreply.github.com>`. `docs/assignment-spec.md`, `.claude/settings.local.json`, `.claude/prompt-history.jsonl`은 `.gitignore`로 제외. 푸시 인증은 저장소 로컬 `credential.https://github.com.helper`가 `gh auth token --user manners-parkpro`를 사용(gh 활성 계정·전역 설정은 그대로)
- 로컬 훅(설정은 `.claude/settings.local.json`): `log-prompt.py`(UserPromptSubmit → `.claude/prompt-history.jsonl`, `<task-notification>`·`<agent-message` 제외 — 그 전에 기록된 4건은 대조할 때 건너뜀), `log-instructions.py`(InstructionsLoaded → `.claude/instructions-log.jsonl`), `guard-git.py`(PreToolUse Bash — 커밋 때 JDK 17 테스트로 약 10초). 세션 중에 만든 에이전트·지침 파일은 그 세션에서 로드되지 않을 수 있다 — `step-reviewer`를 고를 수 없으면 같은 파일 내용으로 general-purpose 에이전트를 쓴다

## 세션 로그 (최신이 위)

### 2026-10-27 · S18 13단계 최종 점검(`/submission-check`)·독립 리뷰 반영·재검증 — 사용자 지시 #16
- 사용자 결정: ZIP 구성 `.git/` 제외·최상위 폴더 `assignment/` 포함, 파일명 `<이름>_springboot.zip`(실명은 공개 기록에 적지 않는다), 위치는 프로젝트 상위 폴더
- SKILL 1~5단계(스크래치 `s18/`):
  - 체크리스트 미완료 12건(⬜ 2·🟨 10, ❌ 0)
  - JDK 21·17 `clean build` 각 45건 통과(실패·오류·건너뜀 0). 콘솔 메시지는 README에 적은 두 가지뿐
  - 실행 스모크(README 명령 그대로, 서명 키 없이, `todo.db` 없는 상태) 19/19: 가입 201 → 로그인 200(두 토큰) → 내 정보 200 → 생성 201·목록 200·수정 200·삭제 204 → 토큰 없음 401 → 없는 ID 404 → 재발급 200(새 쌍) → 재사용 401 → `/v3/api-docs` 200. `.schema` 테이블 3개·unique 2개·인덱스 2개. 응답·로그·DB 덤프에 비밀번호·토큰 원문 0건, WARN 1(서명 키 안내), Ctrl+C 0.4초 종료
  - 정적 점검 6종 통과(금지 기능은 보강 검사 포함). 문서: README 필수 항목, API 표 12개 = `api.md`(성공·오류 코드까지), 프롬프트 원문 대조 누락 없음, `session_start` 로드 기록 확인
- 시험 ZIP 1 전체 실측(`zip-sweep.sh` — S17 `final-sweep.sh`에서 ZIP 생성 분리): ZIP 117개 = Git 추적 116 + 명세 사본, 식별자·토큰 0건, JDK 17·21 테스트 각 45건, `bootRun` 테이블 생성, README 예시 16개(bash·zsh·대화형 붙여 넣기), 스모크 15회·단언 37건(8080·8081), Swagger UI, 기대값 변조 검사 rc 1, Ctrl+C 종료, 로그 ERROR 0
- 독립 리뷰(`step-reviewer`, 시험 ZIP 사본·읽기 전용): 실격·높음·중간 0, 낮음 2·절차 1 — 모두 반영
  - ① 체크리스트 낡은 서술(SEC-03·07 🟨, TEST-05, DOC-08)과 D-17 "미확인" → 근거와 함께 정리
  - ② README 검증 규칙 표가 코드보다 느슨(공백만 있는 필수 문자열, TODO 수정 길이, 회원정보 수정 username 50자) → README·`api.md` 보완, 실서버 18건 실측(경계값 포함)
  - ③ 시험 ZIP이 SKILL 수정보다 먼저 만들어짐 → 최종 ZIP은 기록 커밋 뒤 생성
- SKILL 보완: 인자로 파일명 전체를 받아도 접미사가 한 번만 붙게(`N="${A%_springboot.zip}"`, bash·zsh 실측), argument-hint 예시
- 재검증(시험 ZIP 2 — 리뷰 반영본을 **한글·공백 경로**에 풀어 실행. 파일명 규칙 때문에 Windows "압축 풀기" 기본 경로가 `…\이름_springboot\assignment`가 된다): 시험 ZIP 1과 같은 항목 모두 통과. README 명령 추가 실측: `./gradlew test` 재실행 UP-TO-DATE·`--rerun` 45건, `export TODO_JWT_SECRET=…` 뒤 재시작해도 기존 액세스 토큰 200(대조: 미설정이면 401·재발급 200)
- 실패·오류와 원인(모두 측정 도구 쪽, 앱 결함 0):
  - ① 제외 검사 정규식이 `/out/`을 최상위로 고정하지 않아 헥사고날 `adapter/out/`·`port/out/` 소스 29개를 셌다 → 최상위 고정, 위반 항목을 넣은 ZIP으로 검사가 실제로 걸리는지 확인
  - ② 출력 포맷터 SyntaxError — bash 작은따옴표 안의 `\"`가 그대로 Python 코드에 들어가 줄 연속 문자로 해석됐다(Python 3.14, 버전 문제 아님) → 별도 파일로 분리
  - ③ 이전 실행 로그의 "Started" 줄을 대기 스크립트가 읽어 서버 기동 전에 호출(경쟁 조건, 전부 000) → 기동 전에 로그 삭제
  - ④ macOS bash 3.2가 바깥 큰따옴표 안 `$(…)`의 JSON `{…,…}`를 brace expansion으로 둘로 나눠 가입 400 → 상태 코드를 변수에 먼저 받음. README 예시는 대입 형태라 해당 없음(bash 3.2·5.2·zsh에서 16개 일치)
  - ⑤ 디버그 출력을 `head`로 자르다 SIGPIPE로 스크립트가 끝나 서버 1개가 남았고 다음 기동이 8080 충돌 → 그 서버를 중지 파일로 Ctrl+C 종료, 포트·프로세스 0 확인 뒤 재실행
- 커밋: `a1f88dc`(README 검증 규칙·`api.md`·SKILL), `bc778a7`(기록). CI run 40475001968(`bc778a7`) 6개 조합 성공 — README 예시 16개 일치(macOS bash·zsh, Ubuntu bash), 스모크 8080·8081 각 6/6, 모든 작업에서 테스트 실행, PowerShell 5.1 단계 8개, 경고·오류 0
- 제출 ZIP(GOAL 3, `final-zip.sh` — CI 성공 확인 뒤): 작업 트리 깨끗·HEAD = `origin/main` = `bc778a7` 확인 → SKILL 7단계 명령 그대로 프로젝트 상위 폴더에 `<이름>_springboot.zip` 생성
  - SKILL 확인: 제외 대상 0건, 필수 파일 목록, `gradlew` `-rwxr-xr-x`, `used_prompts.md` 끝이 #16
  - 파일명 NFC(조합형), 265,315바이트, SHA-256 `511add7b…a01a`
  - 파일 117개 = Git 추적 116 + 명세 사본, 내용 = `bc778a7` 체크아웃(시험 ZIP 2와는 기록 파일 4개만 다름)
  - 풀어서(한글·공백 경로) 전체 실측: 시험 ZIP과 같은 항목 모두 통과
- 실패·오류와 원인(이어서): ⑥ ZIP과 HEAD의 CRC 대조에서 `gradlew.bat` 1건 불일치 — `.gitattributes`(`*.bat text eol=crlf`)로 blob은 LF, 체크아웃·ZIP은 CRLF라서 생긴 대조 방법의 오탐. ZIP = 작업 트리 파일 = blob을 CRLF로 바꾼 것(112줄)으로 확인
- 다음: 사용자가 채용 사이트(사람인/잡코리아)에 업로드

### 2026-10-27 · S17 12단계 README 작성·명령 실측, 작성 뒤 전체 재검증 — 사용자 지시 #15
- 작성(커밋 `6d0a20e` README, `1c271a6` CI):
  - `README.md`: 선택한 스택, 실행 방법(준비·서버 실행·JWT 서명 키·실행이 안 될 때), 테스트 실행 방법(필수 4종 ↔ 테스트 클래스), API 목록 요약(12개·검증 규칙·인증·오류 응답·Swagger UI), curl 요청 예시, 프로젝트 구조, 문서
  - CI: `readme-examples.sh`(README 예시를 적힌 그대로 실행해 상태 코드 비교 + 붙여 넣기 함정 정적 검사), `ReadmeSmoke.java`가 README의 API를 모두 호출(15회)·주소 인자, `ci.yml`에 예시 단계(bash·macOS zsh)와 8080 사용 중 다른 포트 실행 단계, Windows 단계는 PowerShell 5.1로 전환
  - 규칙·결정: `docs.md` README 규칙(코드 블록 주석 금지·특수 변수·`?` 따옴표, 예시 CI), D-18 보완, SKILL 정적 점검의 `sqlite3` 검사를 대소문자 구분으로 정정(스택 표의 "SQLite3"가 걸렸다)
- 작성 전 실측(스크래치 `s17/`, 제출 ZIP 명령으로 만든 사본):
  - 빈 Gradle 홈에서 `./gradlew test`: JDK 17 85초(배포판·의존성 내려받기 포함)·JDK 21 32초, 45건 통과, 루트 `todo.db` 없음. JDK 11·8을 `JAVA_HOME`으로 주면 "Gradle requires JVM 17 or later to run"
  - 콘솔 메시지 출처: `Deprecated Gradle features…`는 Spring Boot Gradle 플러그인 `JavaPluginAction`의 `Configuration.setVisible`(Gradle 11 제거 예정, deprecation 스택), `Sharing is only supported…`는 Mockito 인라인 목 메이커(사본에서 subclass 목 메이커로 바꾸면 1줄 → 0줄)
  - `./gradlew bootRun`(`todo.db` 없음): 7초 기동, 루트에 `todo.db`, 테이블 3개·인덱스 2개, WARN은 서명 키 안내 1건, 진행 표시 `80% EXECUTING`. 가상 터미널(pty)에서 Ctrl+C로 0.4초 종료(130)·포트 해제
  - 8080 사용 중: "Port 8080 was already in use"로 BUILD FAILED → `--args='--server.port=8081'`로 기동, API 20회 기대 상태 일치
  - 서명 키: 미설정이면 재시작 뒤 기존 액세스 토큰 401(Invalid signature)·재발급 200, `export TODO_JWT_SECRET=…` 뒤 재시작하면 200, Base64 아님·16바이트는 기동 실패(이유 출력)
  - 실행 권한: `unzip`·`ditto`(Finder)·`tar`는 유지, Python `zipfile`은 제거 → zsh `permission denied`·bash `Permission denied`(126) → `chmod +x gradlew`로 해결
  - 붙여 넣기 함정(기본 대화형 zsh — `/etc/zshrc`에 `interactivecomments` 없음): `#` → `command not found: #`·줄 끝 주석이 붙은 대입 미적용, `USERNAME` 대입 무시, 따옴표 없는 `?` → `no matches found`. bash는 모두 정상
- 작성 뒤 검증(`final-sweep.sh` 2회 — 리뷰 반영 전·후, 매번 새 ZIP·새 사본):
  - ZIP 제외 대상 0건·필수 파일 6/6·`gradlew` `-rwxr-xr-x`, JDK 17·21 `./gradlew test` 45건, `bootRun`만으로 `todo.db`·테이블 생성
  - README 예시: bash 3.2·zsh 5.9 스크립트 실행과 기본 대화형 zsh·bash 붙여 넣기 모두 상태 16개 일치·셸 오류 0, `BASE`를 8081로 바꿔도 일치. 기대값을 바꾼 README, 주석·`!`·`USERNAME=`을 넣은 README는 검사가 실패(rc 1)
  - `ReadmeSmoke` 15회·API 단언 37건(오류 경로 400·401·404·409 포함): 8080(JDK 17)·8081(JDK 21, 같은 폴더에서 동시 실행) 모두 통과
  - Swagger UI(헤드리스 Chrome): 작업 12개·자물쇠 9개, 토큰만 Authorize → 내 정보 200·생성 201·검색 200, 승인 상태 로그인 200(Bearer 미전송)
  - 서버 로그 ERROR 0·WARN 1(서명 키)·토큰·비밀번호 0, Ctrl+C로 두 서버 종료 뒤 포트·프로세스 0. 저장소 JDK 17·21 `clean build` 45건
  - CI(`1c271a6`, run 40442195752): 6개 조합 성공. 모든 작업에서 `> Task :test` 실행, Linux·macOS `.schema` 테이블 3개, 스모크 8080·8081 각 6/6. README 예시 상태 16개 일치(Ubuntu bash 5.2, macOS bash 3.2·zsh 5.9). Windows 단계는 `WindowsPowerShell\v1.0\powershell.EXE`(5.1)로 실행돼 `"--args=--server.port=8081"` 서버 스모크도 통과. `##[warning]`·`##[error]` 0건
- 실패·오류와 원인(끝까지 추적):
  - ① Write 도구가 `ReadmeSmoke.java`의 `\uXXXX`를 한글로 바꿔 저장(S13과 같은 현상) → `file`로 발견해 이스케이프로 되돌리고 ASCII 확인
  - ② pty 측정 첫 시도에서 Ctrl+C가 먹지 않음 — 측정 스크립트를 백그라운드(&)로 띄워 SIGINT 무시 상태가 JVM까지 이어졌다(측정 도구 문제). 자식에서 SIGINT 기본값을 되돌려 재측정. 남은 서버가 8080을 쥐어 다음 기동이 포트 충돌로 실패한 것도 같은 원인
  - ③ Swagger UI 검색 호출 시간 초과 — 파라미터 입력란의 지연 반영(debounce) 전에 Execute를 눌러 "Required field is not provided"(검증 스크립트 문제) → 1초 대기 후 통과
  - ④ SKILL 정적 점검이 README 스택 표의 "SQLite3"에 걸림(대소문자 무시 검색) → CLI 이름은 대소문자 구분으로 정정
- 독립 리뷰(`step-reviewer`): 결함 없음, 낮음 2건 반영 — ① CI 예시 실행이 비대화형이라 붙여 넣기 함정을 못 잡음 → 정적 검사 추가 ② CI·D-18의 예시 실행 범위 과장 → Linux·macOS로 정정. 불확실 사항: Windows PowerShell 5.1 → CI Windows 단계를 5.1로 바꾸고 포트 명령은 인자 전체를 큰따옴표로, "서버 실행 중에도 테스트 가능" 문구는 macOS 실측뿐이라 삭제, Mockito 원인은 실험으로 확정
- 다음: 사용자 확인 후 13단계 `/submission-check <이름>`

### 2026-10-26 · S16 10단계 추가 테스트(TEST-X1·X3·X4), 반복·순서 독립 확인
- 작성: `InvalidTokenTest`(TEST-X1 4건 — 다른 키 서명·sub 변조·alg: none·1초 전 만료), `DuplicateSignupTest`(TEST-X3), `ValidationErrorTest`(TEST-X4 6건 — 가입 4종 파라미터화·TODO 4종·읽을 수 없는 본문·숫자 아닌 ID). 운영 코드 변경 없음. 커밋 `db98c0d`, 리뷰 반영 `1e1e081`
- 실행 명령과 결과:
  - JDK 17 `./gradlew test` → 45건 통과(첫 실행). JDK 17 `cleanTest test` 2회 연속 45/45, 무작위 클래스·메서드 순서(시드 20261026·7) 45/45 두 번 — 결과 XML 시각으로 순서가 실제로 섞였음을 확인, 임시 설정 파일은 삭제
  - JDK 17·21 `clean build` → BUILD SUCCESSFUL, 45건. 테스트 DB 표 0개(create-drop), `todo.db` 미생성
  - 변이 4종(되돌린 뒤 `cmp`): 서명을 검증하지 않는 디코더 → X1 4건 모두 실패, 시계 오차 0 설정 제거 → 1초 전 만료 건만 실패(TEST-07은 1시간 전 만료라 이 회귀를 못 잡음), 가입 중복 확인 제거 → X3 실패, TODO 생성 `@Valid` 제거 → X4 실패. 리뷰 반영 뒤 재실행해도 같음
  - 401 거부 사유 실측: 만료 "Jwt expired at …", 다른 키·변조 "Signed JWT rejected: Invalid signature", alg: none "Unsupported algorithm of none"(디코더의 서명 없는 토큰 거부 경로) — 이 문구를 테스트에 단언
  - CI(`db98c0d`, run 40298369548) 6개 조합 성공·스모크 6/6·경고 0
- 독립 리뷰(`step-reviewer`): 정확성·보안·명세 결함 없음. 낮음 2건 반영 — ① TEST-X4는 필드가 모두 한 단어라 JSON 이름 변환을 구별하지 못함 → 체크리스트 근거를 "요청 필드 이름"으로 좁히고 변환 근거는 OPT-02로 적음 ② TEST-X1 변조 건·TEST-X3이 refresh_token을 읽는 도우미를 써서 확장에 묶임 → access_token 도우미·직접 가입 요청으로 바꿈. 참고 사항(거부 사유 미단언)도 반영
- 다음: 사용자 확인 후 12단계 README(최종 API 기준, 명령 실검증)

### 2026-10-26 · S15 11단계 선택 기능(OPT-01~03) 구현·실측 — 사용자 지시 #14
- 구현 전 재검토(보고 후 진행): D-21을 바꿀 더 나은 방안 없음.
  - 하드 삭제 + AUTOINCREMENT는 방언에서 불가, 요청마다 사용자 상태 조회는 필수 인증 경로 변경, username 해제는 기존 열 NOT NULL을 SQLite가 바꿀 수 없어 기각.
  - 로그인 타이밍 차이는 가입 API가 409로 존재를 알려 주므로 바꾸지 않음.
  - 구현 세부 4가지: 새 비밀번호 공백 거부, 미사용 조회 메서드 정리, 테스트 도우미는 추가만, 기존 DB 열 추가 실측.
- 구현·커밋(기능마다 테스트·회귀·실측 후):
  - OPT-01 검색 `21bd2ba`, OPT-02 수정 `0336d16`, OPT-03 탈퇴 `b20471c`.
  - 리뷰 반영: `f923d77`(요청 DTO 이름 `SearchTodoRequest`), `6b5bec6`(새 비밀번호 72바이트 테스트).
- 실행 명령과 결과:
  - JDK 17 `./gradlew test`: OPT-01 뒤 23건, OPT-02 뒤 29건, OPT-03 뒤 34건 통과. JDK 21 `clean build`도 기능마다 같은 건수 통과. CI 3개 OS × JDK 17·21은 기능 커밋 3개 모두 성공(스모크 6/6, 경고 0).
  - 변이 17종(되돌린 뒤 `cmp`): 16종 검출. `IgnoreCase` 제거는 통과 — SQLite `LIKE`가 원래 ASCII 대소문자를 구분하지 않음(D-21).
  - bootRun 실측(스크래치 `opt01-smoke.sh`·`opt02-smoke.sh`·`opt03-smoke.sh`·`rollback-check.sh`):
    - 검색 SQL에 `escape '\'`, 다른 사용자 TODO 제외, `%`·`_` 문자 그대로.
    - 수정 뒤 DB 해시 교체·옛 리프레시 토큰 401, 현재 비밀번호 틀림 400이 중복 409보다 먼저.
    - 탈퇴 뒤 로그인·재발급 401·`/users/me` 404·TODO 0행·같은 username 409. 마지막 가입자(id 2)가 탈퇴한 뒤 새 가입자는 id 3, 탈퇴자 토큰은 404.
    - OPT-03 이전 버전이 만든 `todo.db`에 새 버전 실행 → `alter table users add column withdrawn boolean default false not null`, 기존 계정 로그인·TODO 조회·탈퇴 정상.
    - 리스너가 실패하면 탈퇴 요청 500(일반 본문)이고 `withdrawn`·리프레시 토큰·TODO 모두 롤백.
    - 로그에 ERROR·토큰·비밀번호 0건.
  - 정적 점검: 도메인 프레임워크 import 0, web → domain 0, application의 Spring 웹·이벤트·JPA import 0, user ↔ todo 직접 import 0/0. 필수 테스트 파일 4개 변경 0건, 테스트 도우미 삭제 줄 0.
- 실패와 원인(끝까지 추적):
  - ① OPT-02 첫 실행 `compileTestJava` 실패 — `ApiDocsTest`의 `Map.of`는 키·값 10쌍까지만 받는데 API가 11개가 됐다 → `Map.ofEntries`로 바꿔 해결(탈퇴 추가로 12개).
  - ② 리뷰 반영 커밋 명령 실패 — `git mv`가 이미 스테이징한 옛 경로를 다시 `git add`해 pathspec 오류, 이어서 메시지 파일 생성 단계가 실행되지 않음 → 스테이징 확인 후 다시 커밋.
  - ③ 실측 스크립트 표시 오류 — `grep -o '[^\r]*'`가 "백슬래시나 r이 아닌 문자"로 해석돼 `withdrawn`의 r에서 잘림. 실제 ALTER 문은 로그 원문으로 확인.
- 독립 리뷰(`step-reviewer`): 기능·보안 결함 없음. 지적 1건(요청 DTO 이름) 반영. 참고 사항도 반영: 새 비밀번호 72바이트 테스트 추가, 탈퇴 뒤 남은 토큰으로 만든 TODO가 남는 한계를 D-21·security.md에 명시, 리뷰어가 확인하지 못한 기존 DB 열 추가·리스너 실패 롤백은 실측.
- 다음: 10단계(TEST-X1·X3·X4, 2회 연속 실행, `./gradlew build`)

### 2026-10-26 · S13~S14 요약 (세부는 이 파일의 git 이력)
- S13 9단계 전 명세 전수 재대조(변경 없음). 다른 프로젝트용 `JWT_SECRET`이 셸에 있으면 기동이 실패하는 위험을 재현해 환경 변수를 `TODO_JWT_SECRET`으로 바꿈(D-06, R-11). Windows 기본 인코딩(cp1252) 흉내 빌드·한국어 응답 정상. CI 추가 — Windows·Linux·macOS × JDK 17·21에서 README 명령과 스모크 10회(D-18, R-12), Windows 캐시 저장 경고는 서버 종료 단계로 해소
- S14 9단계 Swagger(API-10): `OpenApiConfig`(제목·Bearer 스키마·태그 순서), 보호 API에만 `@SecurityRequirement`, `POST /todos` 201 문서화, springdoc 기본 활성 WARN 제거, 헤드리스 Chrome으로 Authorize → 보호 API 호출 확인, 리뷰 4건 반영. #13: 제출 ZIP 제외 항목 실측(`build/`·`.gradle/`·`target/`·`*.db` 빠짐, `gradlew` 실행 권한 유지), 선택 사항 설계 D-21(탈퇴는 탈퇴 표시 — SQLite ID 재사용 재현, R-13), 평가 기준·실격 조건 점검

### 2026-10-25~26 · S9~S12 요약 (세부는 이 파일의 git 이력)
- S9 6단계 User·Auth(TEST-01): 가입·로그인(`access_token`만)·내 정보, bcrypt 72바이트 검증·로그인 실패 같은 401, 어댑터 package-private. JDK 17·21 9건 통과, bootRun으로 HS256·`exp-iat=3600`·`{bcrypt}` 저장값 확인
- S10 명세 재대조(블록 76개 동일)·7단계 TODO(TEST-02·04·X2): 소유권 404 은닉, SQLite ID 재사용 재현(R-7), 동시 요청 120건(R-3 해소), 사용자 지시로 Result → Response 변환·참고 프로젝트 네이밍(D-20). 14건 통과
- S11 테이블은 기동 때 `ddl-auto`로 생성(실행 방법과 무관) 실측, JDK 17~27 실행 범위 점검 — JDK 27에서 Lombok 1.18.46 컴파일 실패 발견(R-9)
- S12 협의 반영(Lombok 1.18.48·`memory-bank/` 제외), 8단계 재발급 확장(TEST-07·08·09): 회전·재사용 탐지(`noRollbackFor`)·만료, 변이 4종, 서버 재시작 뒤 재발급 실측, TTL 위치 검토(yml 유지). 17건 통과

### 2026-10-25 · S1~S8 요약 (세부는 이 파일의 git 이력)
- S1 설계 검증과 작업 체계 수립: CLAUDE.md 초안 458줄 → 72줄, rules·skill·docs 작성. Boot 4.1.2·Gradle 9.8.2 생성물, SQLite FK DDL 미생성, BCrypt 72바이트 예외, NimbusJwtEncoder 기본 RS256, ERROR 디스패치 401 함정 확인
- S2 명세 원문 수집(블록 76개)·대조, 사용자 결정 3건(빌드 기준선·Java 17 타깃·springdoc 유지), `memory-bank/` 백업 후 삭제, `git init`
- S3 저장소 정리·원격 연결, 테이블 생성 경로(D-10)·재발급 확장(D-15) 설계
- S4 빌드 기준선 교정(Boot 3.5.16·`release 17`·Tomcat 10.1.61·springdoc 2.8.17), 커밋 작성자를 닉네임 + noreply로 재작성, 프롬프트 기록 훅·백필(D-16)
- S5 저장소 재생성 확인, 푸시는 저장소 로컬 헬퍼(`gh auth token --user manners-parkpro`), JDK 17 기본 + 21 교차 확인(사용자 결정)
- S6 4단계 설정·테스트 기반(SQLite·풀 1·`ddl-auto: update`, 테스트 DB `build/todo-test.db`). springdoc 문서 엔드포인트 기본 활성 WARN 2건은 9단계에서 처리 검토
- S7 압축 뒤 프롬프트 기록·CLAUDE.md 로드 검증, 훅 추가(`log-instructions.py`·`guard-git.py`), `step-reviewer`, 설계 문서 리팩터링(독립 리뷰·best practices 대조)
- S8 사용자 참조 이름 `userId` 결정, 5단계 보안 골격(TEST-03) — ProblemDetail 엔트리 포인트, 공개 인증 경로 Bearer 헤더 무시, 기본 `logout` 필터 비활성(리뷰 지적 재현 후)
