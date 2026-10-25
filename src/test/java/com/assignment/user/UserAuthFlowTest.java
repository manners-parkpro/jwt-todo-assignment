package com.assignment.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class UserAuthFlowTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-01] 회원가입 → 로그인 → 발급받은 토큰으로 /users/me 조회 성공")
	void signupLoginAndGetMe() throws Exception {
		String username = "user-" + UUID.randomUUID();
		String credentials = """
				{"username": "%s", "password": "password1234"}
				""".formatted(username);

		String signupResponse = mockMvc.perform(post("/users/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.username").value(username))
				.andReturn().getResponse().getContentAsString();
		Long userId = ((Number) JsonPath.read(signupResponse, "$.id")).longValue();

		// 필수 흐름은 access_token만 사용한다(리프레시 토큰은 확장 기능이라 의존하지 않는다)
		String loginResponse = mockMvc.perform(post("/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isString())
				.andReturn().getResponse().getContentAsString();
		String accessToken = JsonPath.read(loginResponse, "$.access_token");

		mockMvc.perform(get("/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.password").doesNotExist());
	}
}
