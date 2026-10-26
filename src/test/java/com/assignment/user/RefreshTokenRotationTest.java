package com.assignment.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class RefreshTokenRotationTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-08] 재발급할 때마다 리프레시 토큰이 바뀌고, 이미 쓴 토큰을 다시 쓰면 401 — 그 뒤에는 최신 토큰도 401")
	void rotationAndReuseDetection() throws Exception {
		String first = signupAndLoginWithRefreshToken().refreshToken();

		String refreshed = refresh(first)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String second = JsonPath.read(refreshed, "$.refresh_token");
		assertThat(second).isNotEqualTo(first);

		// 이미 쓴 토큰이 다시 오면 탈취로 보고 그 사용자의 리프레시 토큰을 모두 폐기한다
		refresh(first)
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("리프레시 토큰이 유효하지 않습니다."));
		refresh(second)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("리프레시 토큰이 유효하지 않습니다."));
	}
}
