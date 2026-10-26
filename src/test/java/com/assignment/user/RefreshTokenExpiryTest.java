package com.assignment.user;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

// 리프레시 토큰 수명을 0초로 둬 발급 즉시 만료되게 한다. 설정이 달라 Spring 컨텍스트가 따로 뜨므로,
// create-drop이 다른 테스트 컨텍스트의 테이블을 지우지 않도록 DB 파일도 따로 쓴다
@TestPropertySource(properties = {
		"auth.refresh-token-ttl=0s",
		"spring.datasource.url=jdbc:sqlite:build/todo-test-refresh-expiry.db"
})
class RefreshTokenExpiryTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-09] 만료된 리프레시 토큰과 없는 토큰도 이미 쓴 토큰과 같은 401")
	void expiredOrUnknownRefreshTokenIsUnauthorized() throws Exception {
		String expired = signupAndLoginWithRefreshToken().refreshToken();

		refresh(expired)
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
				.andExpect(jsonPath("$.detail").value("리프레시 토큰이 유효하지 않습니다."));
		refresh("unknown-refresh-token")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("리프레시 토큰이 유효하지 않습니다."));
	}
}
