package com.assignment.user.application.service;

import com.assignment.user.application.InvalidCredentialsException;
import com.assignment.user.application.InvalidRefreshTokenException;
import com.assignment.user.application.dto.LoginCommand;
import com.assignment.user.application.dto.RefreshCommand;
import com.assignment.user.application.dto.TokenResult;
import com.assignment.user.application.port.in.AuthUseCase;
import com.assignment.user.application.port.out.AccessTokenPort;
import com.assignment.user.application.port.out.PasswordHashPort;
import com.assignment.user.application.port.out.RefreshTokenCommandPort;
import com.assignment.user.application.port.out.RefreshTokenQueryPort;
import com.assignment.user.application.port.out.UserQueryPort;
import com.assignment.user.domain.RefreshToken;
import com.assignment.user.domain.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthUseCase {

	// 256비트 난수라 추측할 수 없으므로 저장할 때는 느린 해시 대신 SHA-256으로 충분하다
	private static final int REFRESH_TOKEN_BYTES = 32;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final UserQueryPort userQueryPort;
	private final PasswordHashPort passwordHashPort;
	private final AccessTokenPort accessTokenPort;
	private final RefreshTokenCommandPort refreshTokenCommandPort;
	private final RefreshTokenQueryPort refreshTokenQueryPort;
	private final Duration refreshTokenTtl;

	public AuthService(UserQueryPort userQueryPort, PasswordHashPort passwordHashPort,
			AccessTokenPort accessTokenPort, RefreshTokenCommandPort refreshTokenCommandPort,
			RefreshTokenQueryPort refreshTokenQueryPort, @Value("${auth.refresh-token-ttl}") Duration refreshTokenTtl) {
		this.userQueryPort = userQueryPort;
		this.passwordHashPort = passwordHashPort;
		this.accessTokenPort = accessTokenPort;
		this.refreshTokenCommandPort = refreshTokenCommandPort;
		this.refreshTokenQueryPort = refreshTokenQueryPort;
		this.refreshTokenTtl = refreshTokenTtl;
	}

	@Override
	@Transactional
	public TokenResult login(LoginCommand command) {
		User user = userQueryPort.findByUsername(command.username())
				.filter(found -> passwordHashPort.matches(command.rawPassword(), found.getPassword()))
				.orElseThrow(InvalidCredentialsException::new);
		Instant now = Instant.now();
		refreshTokenCommandPort.deleteExpiredByUserId(user.getId(), now);
		return issueTokens(user.getId(), now);
	}

	// 재사용을 탐지해 폐기한 뒤 401 예외를 던져도 폐기가 커밋되도록 이 예외로는 롤백하지 않는다.
	// 예외는 이 메서드 안에서 직접 던진다 — 다른 @Transactional 경계를 거치면 트랜잭션이 rollback-only가 된다(D-15)
	@Override
	@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
	public TokenResult refresh(RefreshCommand command) {
		RefreshToken refreshToken = refreshTokenQueryPort.findByTokenHash(hash(command.refreshToken()))
				.orElseThrow(InvalidRefreshTokenException::new);
		if (refreshToken.isUsed()) {
			// 이미 쓴 토큰이 다시 왔다 — 탈취로 보고 그 사용자의 리프레시 토큰을 모두 폐기한다(RFC 9700 §4.14.2)
			refreshTokenCommandPort.deleteAllByUserId(refreshToken.getUserId());
			throw new InvalidRefreshTokenException();
		}
		Instant now = Instant.now();
		if (refreshToken.isExpired(now)) {
			throw new InvalidRefreshTokenException();
		}
		refreshToken.markUsed();
		refreshTokenCommandPort.save(refreshToken);
		return issueTokens(refreshToken.getUserId(), now);
	}

	// 원문은 응답으로 한 번만 내보내고, 저장소에는 해시만 남긴다
	private TokenResult issueTokens(Long userId, Instant now) {
		String rawRefreshToken = generateRefreshToken();
		refreshTokenCommandPort.save(RefreshToken.issue(userId, hash(rawRefreshToken), now.plus(refreshTokenTtl)));
		return new TokenResult(accessTokenPort.issue(userId), rawRefreshToken);
	}

	private static String generateRefreshToken() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String hash(String rawRefreshToken) {
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(sha256.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// Java 플랫폼은 SHA-256을 반드시 제공한다(MessageDigest 명세)
			throw new IllegalStateException(e);
		}
	}
}
