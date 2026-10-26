package com.assignment.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiDocsTest extends IntegrationTestSupport {

	// 보호 API의 security 값은 [{"bearerAuth": []}]이어야 Swagger UI가 Authorize한 토큰을 보낸다
	private static final List<Map<String, List<String>>> BEARER_AUTH_REQUIREMENT =
			List.of(Map.of(OpenApiConfig.BEARER_AUTH, List.of()));

	@Test
	@DisplayName("[API-10] 보호 API에만 Bearer 인증 요구를 표시한다")
	void documentsBearerAuthOnlyOnProtectedApis() throws Exception {
		String apiDocs = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.%s.type", OpenApiConfig.BEARER_AUTH).value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.%s.scheme", OpenApiConfig.BEARER_AUTH).value("bearer"))
				.andReturn().getResponse().getContentAsString();

		// 문서의 모든 API를 모아 비교하므로 보안 표시가 빠지거나 다른 스키마를 가리키는 API가 생기면 여기서 드러난다
		Map<String, Map<String, Map<String, Object>>> paths = JsonPath.read(apiDocs, "$.paths");
		Map<String, Boolean> requiresBearer = new HashMap<>();
		paths.forEach((path, operations) -> operations.forEach((method, operation) -> {
			boolean bearerRequired = BEARER_AUTH_REQUIREMENT.equals(operation.get("security"));
			requiresBearer.put(method.toUpperCase() + " " + path, bearerRequired);
		}));

		assertThat(requiresBearer).isEqualTo(Map.of(
				"POST /users/signup", false,
				"POST /users/login", false,
				"POST /users/refresh", false,
				"GET /users/me", true,
				"POST /todos", true,
				"GET /todos", true,
				"GET /todos/search", true,
				"GET /todos/{id}", true,
				"PUT /todos/{id}", true,
				"DELETE /todos/{id}", true));
	}

	@Test
	@DisplayName("[API-10] 실제 응답과 같은 성공 상태 코드와 토큰 필드 이름을 문서화한다")
	void documentsActualStatusCodesAndTokenFieldNames() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/todos'].post.responses['201']").exists())
				.andExpect(jsonPath("$.paths['/todos'].post.responses['200']").doesNotExist())
				.andExpect(jsonPath("$.paths['/todos/{id}'].delete.responses['204']").exists())
				.andExpect(jsonPath("$.components.schemas.TokenResponse.properties.access_token").exists())
				.andExpect(jsonPath("$.components.schemas.TokenResponse.properties.refresh_token").exists())
				.andExpect(jsonPath("$.components.schemas.RefreshRequest.required[0]").value("refresh_token"));
	}
}
