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
- Boot 3.5의 공식 지원 목록은 Gradle 7.6.4+/8.4+다. 9.8.2 조합은 기준선 단계에서 실측한다(아래 검증). 실패하면 8.14.6으로 내리고 README에 "JDK 17~24"를 명시한다.

## 의존성
- 버전은 Boot BOM에 맡기고 적지 않는다. 예외: BOM에 없는 `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17`(Boot 3.5용 2.8.x).
- 구성: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-oauth2-resource-server`, `runtimeOnly 'org.xerial:sqlite-jdbc'`, `implementation 'org.hibernate.orm:hibernate-community-dialects'`, Lombok(main만: `compileOnly` + `annotationProcessor`). 테스트: `spring-boot-starter-test` + `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'`.
- Boot 4 전용 아티팩트(`spring-boot-starter-webmvc`, `*-test` 모듈 스타터)와 springdoc 3.x는 쓰지 않는다.
- 보안 패치 오버라이드: `ext['tomcat.version'] = '10.1.61'`. Boot 3.5의 OSS 지원이 끝난 뒤에도 Apache가 OSS로 패치하는 Tomcat만 올린다. Spring 계열은 OSS 패치가 없으므로 `security.md`의 사용 금지 기능으로 대응한다.
- 새 의존성은 추가하기 전에 `docs/decisions.md`에 필요성을 남긴다.

## 검증
```bash
./gradlew --version                                                                   # Gradle 9.8.2
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test --warning-mode all     # 명세 최소 JDK
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew clean build --warning-mode all
./gradlew dependencyInsight --dependency tomcat-embed-core --configuration runtimeClasspath   # 10.1.61
javap -v -cp build/classes/java/main com.assignment.AssignmentApplication | grep 'major version'  # 61 = Java 17
```
