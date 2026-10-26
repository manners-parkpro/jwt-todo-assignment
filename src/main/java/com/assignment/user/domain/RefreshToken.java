package com.assignment.user.domain;

import java.time.Instant;
import lombok.Getter;

// 리프레시 토큰은 원문이 아니라 해시만 갖는다. 재발급에 한 번 쓰면 used가 되고, 쓴 토큰이 다시 오면 재사용(탈취)으로 본다
@Getter
public class RefreshToken {

	private final Long id;
	private final Long userId;
	private final String tokenHash;
	private final Instant expiresAt;
	private boolean used;

	private RefreshToken(Long id, Long userId, String tokenHash, Instant expiresAt, boolean used) {
		if (userId == null || expiresAt == null) {
			throw new IllegalArgumentException("리프레시 토큰의 사용자 ID와 만료 시각은 비어 있을 수 없습니다.");
		}
		if (tokenHash == null || tokenHash.isBlank()) {
			throw new IllegalArgumentException("리프레시 토큰 해시는 비어 있을 수 없습니다.");
		}
		this.id = id;
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.used = used;
	}

	public static RefreshToken issue(Long userId, String tokenHash, Instant expiresAt) {
		return new RefreshToken(null, userId, tokenHash, expiresAt, false);
	}

	public static RefreshToken of(Long id, Long userId, String tokenHash, Instant expiresAt, boolean used) {
		return new RefreshToken(id, userId, tokenHash, expiresAt, used);
	}

	// 만료 시각과 같아지는 순간부터 만료로 본다(정리 쿼리도 같은 기준)
	public boolean isExpired(Instant now) {
		return !now.isBefore(expiresAt);
	}

	public void markUsed() {
		this.used = true;
	}
}
