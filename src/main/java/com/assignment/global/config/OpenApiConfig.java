package com.assignment.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Swagger UI(/swagger-ui.html)에서 Authorize에 access_token을 넣으면 보호 API를 바로 호출해 볼 수 있다.
// 태그는 과제 명세의 API 묶음(사용자 → TODO) 순서로 보여 준다
@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "JWT TODO API",
				version = "v1",
				description = """
						1. `POST /users/signup`으로 가입하고 `POST /users/login`으로 `access_token`을 받는다.
						2. Authorize에 `access_token`을 넣으면 자물쇠가 표시된 보호 API를 호출할 수 있다(`Authorization: Bearer <access_token>`).
						3. 액세스 토큰이 만료되면 가장 최근에 받은 `refresh_token`(로그인 또는 재발급 응답)으로 `POST /users/refresh`를 호출해 새 토큰 쌍을 받는다. `refresh_token`은 재발급할 때마다 바뀌고, 이미 쓴 토큰을 다시 보내면 그 사용자의 `refresh_token`이 모두 폐기된다.

						오류는 RFC 9457 ProblemDetail(`application/problem+json`)로 응답한다 — 400 입력 검증 실패, 401 인증 실패, 404 없는 TODO(다른 사용자의 TODO 포함), 409 username 중복."""),
		tags = {
				@Tag(name = "사용자"),
				@Tag(name = "TODO", description = "로그인한 사용자 본인의 TODO만 다룬다")
		})
@SecurityScheme(
		name = OpenApiConfig.BEARER_AUTH,
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT")
public class OpenApiConfig {

	// 보호 API의 @SecurityRequirement가 참조하는 보안 스키마 이름 — 다르면 Swagger UI가 토큰을 보내지 않는다
	public static final String BEARER_AUTH = "bearerAuth";
}
