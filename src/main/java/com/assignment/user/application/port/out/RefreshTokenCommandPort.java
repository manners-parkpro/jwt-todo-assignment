package com.assignment.user.application.port.out;

import com.assignment.user.domain.RefreshToken;
import java.time.Instant;

public interface RefreshTokenCommandPort {

	RefreshToken save(RefreshToken refreshToken);

	void deleteAllByUserId(Long userId);

	// 만료 시각이 now 이하인 행을 지운다(RefreshToken.isExpired와 같은 기준)
	void deleteExpiredByUserId(Long userId, Instant now);
}
