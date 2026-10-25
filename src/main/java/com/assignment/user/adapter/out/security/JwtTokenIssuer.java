package com.assignment.user.adapter.out.security;

import com.assignment.user.application.port.out.TokenIssuer;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class JwtTokenIssuer implements TokenIssuer {

	// NimbusJwtEncoder의 기본 헤더는 RS256이라 HS256을 명시하지 않으면 서명 키를 고르지 못해 발급이 실패한다
	private static final JwsHeader HS256_HEADER = JwsHeader.with(MacAlgorithm.HS256).build();

	private final JwtEncoder jwtEncoder;
	private final Duration accessTokenTtl;

	JwtTokenIssuer(JwtEncoder jwtEncoder, @Value("${auth.access-token-ttl}") Duration accessTokenTtl) {
		this.jwtEncoder = jwtEncoder;
		this.accessTokenTtl = accessTokenTtl;
	}

	@Override
	public String issueAccessToken(Long userId) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(String.valueOf(userId))
				.issuedAt(now)
				.expiresAt(now.plus(accessTokenTtl))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(HS256_HEADER, claims)).getTokenValue();
	}
}
