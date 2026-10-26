package com.assignment.global.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
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
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class InvalidTokenTest extends IntegrationTestSupport {

	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

	@Autowired
	private JwtEncoder jwtEncoder;

	@Test
	@DisplayName("[TEST-X1] 다른 키로 서명한 토큰은 401")
	void tokenSignedWithAnotherKeyIsUnauthorized() throws Exception {
		long userId = myId(signupAndLogin());
		byte[] anotherKey = new byte[32];
		new SecureRandom().nextBytes(anotherKey);
		JwtEncoder forger = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(anotherKey, "HmacSHA256")));

		expectInvalidToken(encode(forger, userId, Instant.now().plus(Duration.ofHours(1))), "Invalid signature");
	}

	@Test
	@DisplayName("[TEST-X1] 서명은 그대로 두고 sub(사용자 ID)를 다른 사용자로 바꾼 토큰은 401")
	void tamperedSubjectIsUnauthorized() throws Exception {
		String accessToken = signupAndLogin();
		long otherUserId = myId(signupAndLogin());
		String[] parts = accessToken.split("\\.");
		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), UTF_8);
		String tamperedPayload = payload.replaceFirst("\"sub\":\"\\d+\"", "\"sub\":\"" + otherUserId + "\"");
		assertThat(tamperedPayload).isNotEqualTo(payload);

		expectInvalidToken(parts[0] + "." + BASE64_URL.encodeToString(tamperedPayload.getBytes(UTF_8)) + "." + parts[2],
				"Invalid signature");
	}

	@Test
	@DisplayName("[TEST-X1] 서명이 없는 토큰(alg: none)은 401")
	void unsignedTokenIsUnauthorized() throws Exception {
		long userId = myId(signupAndLogin());
		long now = Instant.now().getEpochSecond();
		String header = BASE64_URL.encodeToString("{\"alg\":\"none\"}".getBytes(UTF_8));
		String payload = BASE64_URL.encodeToString("""
				{"sub":"%d","iat":%d,"exp":%d}""".formatted(userId, now, now + 3600).getBytes(UTF_8));

		expectInvalidToken(header + "." + payload + ".", "Unsupported algorithm of none");
	}

	@Test
	@DisplayName("[TEST-X1] 서버 키로 서명했어도 만료된 토큰은 401")
	void expiredTokenIsUnauthorized() throws Exception {
		long userId = myId(signupAndLogin());

		// 1초 전에 만료 — 시계 오차를 기본값(60초)으로 두면 통과해 버리므로 오차 0 설정을 지킨다
		expectInvalidToken(encode(jwtEncoder, userId, Instant.now().minus(Duration.ofSeconds(1))), "expired");
	}

	private static String encode(JwtEncoder encoder, long userId, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(String.valueOf(userId))
				.issuedAt(expiresAt.minus(Duration.ofHours(1)))
				.expiresAt(expiresAt)
				.build();
		JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
	}

	// 거부 사유(error_description)까지 확인해, 다른 이유로 401이 나서 우연히 통과하지 않게 한다
	private void expectInvalidToken(String token, String reason) throws Exception {
		mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
						allOf(containsString("error=\"invalid_token\""), containsString(reason))))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(401));
	}
}
