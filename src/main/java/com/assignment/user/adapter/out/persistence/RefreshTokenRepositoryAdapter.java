package com.assignment.user.adapter.out.persistence;

import com.assignment.user.application.port.out.RefreshTokenCommandPort;
import com.assignment.user.application.port.out.RefreshTokenQueryPort;
import com.assignment.user.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RefreshTokenRepositoryAdapter implements RefreshTokenCommandPort, RefreshTokenQueryPort {

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public RefreshToken save(RefreshToken refreshToken) {
		return refreshTokenRepository.save(RefreshTokenJpaEntity.from(refreshToken)).toDomain();
	}

	@Override
	public void deleteAllByUserId(Long userId) {
		refreshTokenRepository.deleteAllByUserId(userId);
	}

	@Override
	public void deleteExpiredByUserId(Long userId, Instant now) {
		refreshTokenRepository.deleteAllByUserIdAndExpiresAtLessThanEqual(userId, now.toEpochMilli());
	}

	@Override
	public Optional<RefreshToken> findByTokenHash(String tokenHash) {
		return refreshTokenRepository.findByTokenHash(tokenHash).map(RefreshTokenJpaEntity::toDomain);
	}
}
