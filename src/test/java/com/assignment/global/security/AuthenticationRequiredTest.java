package com.assignment.global.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

class AuthenticationRequiredTest extends IntegrationTestSupport {

	@DisplayName("[TEST-03] 토큰 없이 보호 API를 호출하면 401")
	@ParameterizedTest(name = "[TEST-03] 토큰 없이 {0} {1} → 401")
	@CsvSource({
			"GET, /users/me",
			"POST, /todos",
			"GET, /todos",
			"GET, /todos/1",
			"PUT, /todos/1",
			"DELETE, /todos/1"
	})
	void requestWithoutTokenIsUnauthorized(String method, String path) throws Exception {
		mockMvc.perform(request(HttpMethod.valueOf(method), path))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.instance").value(path));
	}

	@Test
	@DisplayName("[TEST-03] 형식이 잘못된 토큰으로 호출해도 401")
	void requestWithMalformedTokenIsUnauthorized() throws Exception {
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401));
	}
}
