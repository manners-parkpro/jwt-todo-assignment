package com.assignment.user;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class TokenRefreshFlowTest extends IntegrationTestSupport {

	@Autowired
	private JwtEncoder jwtEncoder;

	@Test
	@DisplayName("[TEST-07] 액세스 토큰 만료 → 보호 API 401 → 만료 토큰을 헤더에 단 채 재발급 200 → 새 토큰으로 같은 API 200")
	void refreshAfterAccessTokenExpires() throws Exception {
		Tokens tokens = signupAndLoginWithRefreshToken();
		String me = mockMvc.perform(get("/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		long userId = ((Number) JsonPath.read(me, "$.id")).longValue();
		String expiredAccessToken = expiredAccessTokenFor(userId);

		// 만료된 액세스 토큰으로는 보호 API가 401이다
		mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredAccessToken))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")));

		// 클라이언트가 만료 토큰을 헤더에 그대로 실어 보내도 재발급 경로는 헤더를 무시한다
		String refreshed = mockMvc.perform(post("/users/refresh")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredAccessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"refresh_token": "%s"}
								""".formatted(tokens.refreshToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isString())
				.andExpect(jsonPath("$.refresh_token").isString())
				.andReturn().getResponse().getContentAsString();
		String newAccessToken = JsonPath.read(refreshed, "$.access_token");

		mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId));
	}

	// 서버의 서명 키로 서명하되 exp가 이미 지난 토큰을 만든다(clock skew 0이라 곧바로 만료로 판정된다)
	private String expiredAccessTokenFor(long userId) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(String.valueOf(userId))
				.issuedAt(now.minus(Duration.ofHours(2)))
				.expiresAt(now.minus(Duration.ofHours(1)))
				.build();
		JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
	}
}
