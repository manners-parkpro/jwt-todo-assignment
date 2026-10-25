---
paths:
  - "build.gradle"
  - "settings.gradle"
  - "gradle/**"
  - "gradlew"
  - "gradlew.bat"
---

# 빌드 규칙 (근거 D-01·D-02)

## 기준선
- Spring Boot `3.5.16` + `io.spring.dependency-management`, Gradle Wrapper `9.8.2`(교체하지 않는다).
- Java: `tasks.withType(JavaCompile).configureEach { options.release = 17 }`. `java.toolchain`은 쓰지 않는다 — toolchain은 같은 버전의 JDK가 설치돼 있어야 해서, JDK 버전을 알 수 없는 채점 환경에서 빌드가 실패할 수 있다.
- Boot 3.5의 공식 지원 목록은 Gradle 7.6.4+/8.4+지만, 9.8.2 조합은 JDK 17·21에서 `clean build`까지 실측 통과했다(D-01). Boot 플러그인의 deprecation 경고 1건(Gradle 11 제거 예정 API)은 영향이 없다.

## 의존성
- 버전은 Boot BOM에 맡기고 적지 않는다. 예외: BOM에 없는 `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`(Boot 3.5용 2.8.x).
- 현재 구성은 `build.gradle`이 기준이다. Lombok은 main에만 쓰고(`compileOnly` + `annotationProcessor`) 테스트 의존성으로 추가하지 않는다.
- Boot 4 전용 아티팩트(`spring-boot-starter-webmvc`, `*-test` 모듈 스타터)와 springdoc 3.x는 쓰지 않는다.
- 보안 패치 오버라이드: `ext['tomcat.version'] = '10.1.61'`. Boot 3.5의 OSS 지원이 끝난 뒤에도 Apache가 OSS로 패치하는 Tomcat만 올린다. Spring 계열은 OSS 패치가 없으므로 `security.md`의 사용 금지 기능으로 대응한다.
- 새 의존성은 추가하기 전에 `docs/decisions.md`에 필요성을 남긴다.

## 검증
```bash
# 셸 환경 변수는 Bash 호출마다 초기화되므로 JDK는 명령마다 접두로 지정한다
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew --version                         # Gradle 9.8.2, Launcher JVM 17 (java -version은 PATH의 21)
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test --warning-mode all     # 기본 검증(명세 최소·타깃 JDK)
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew clean build --warning-mode all    # 단계 완료·제출 전 교차 확인
./gradlew dependencyInsight --dependency tomcat-embed-core --configuration runtimeClasspath   # 10.1.61
javap -v -cp build/classes/java/main com.assignment.AssignmentApplication | grep 'major version'  # 61 = Java 17
```
