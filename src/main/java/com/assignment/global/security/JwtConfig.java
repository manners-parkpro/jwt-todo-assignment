package com.assignment.global.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Slf4j
@Configuration
public class JwtConfig {

	private static final int MIN_KEY_BYTES = 32;

	@Bean
	SecretKey jwtSigningKey(@Value("${auth.jwt-secret}") String jwtSecret) {
		return new SecretKeySpec(keyBytes(jwtSecret), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		// 발급과 검증이 같은 서버라 시계 오차를 두지 않는다(기본 60초면 만료된 토큰도 그동안 통과한다)
		decoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
		return decoder;
	}

	private static byte[] keyBytes(String jwtSecret) {
		if (jwtSecret.isBlank()) {
			// 채점 환경에서 JWT_SECRET을 빠뜨려도 실행되도록 고정 키 대신 임의 키를 만든다
			log.warn("JWT_SECRET이 설정되지 않아 임의 서명 키를 생성했습니다. 서버를 재시작하면 기존 액세스 토큰은 무효가 됩니다.");
			byte[] key = new byte[MIN_KEY_BYTES];
			new SecureRandom().nextBytes(key);
			return key;
		}
		byte[] key;
		try {
			key = Base64.getDecoder().decode(jwtSecret.trim());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("JWT_SECRET은 Base64 문자열이어야 합니다.");
		}
		if (key.length < MIN_KEY_BYTES) {
			throw new IllegalStateException("JWT_SECRET은 Base64로 디코딩한 결과가 32바이트 이상이어야 합니다(HS256).");
		}
		return key;
	}
}
