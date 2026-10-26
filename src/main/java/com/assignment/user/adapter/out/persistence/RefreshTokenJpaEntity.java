package com.assignment.user.adapter.out.persistence;

import com.assignment.user.domain.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// SQLite에서는 FK가 생성되지 않으므로 사용자는 ID 컬럼으로만 참조하고, 사용자별 정리·폐기를 위해 인덱스를 둔다
@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "idx_refresh_tokens_user_id", columnList = "userId"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RefreshTokenJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	// 원문이 아니라 SHA-256 해시(16진수 64자)만 저장한다. SQLite에서는 컬럼의 unique만 CREATE TABLE에 들어간다
	@Column(nullable = false, unique = true, length = 64)
	private String tokenHash;

	// SQLite 날짜 타입을 피하려고 epoch 밀리초로 저장한다
	@Column(nullable = false)
	private Long expiresAt;

	@Column(nullable = false)
	private boolean used;

	private RefreshTokenJpaEntity(Long id, Long userId, String tokenHash, Long expiresAt, boolean used) {
		this.id = id;
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.used = used;
	}

	static RefreshTokenJpaEntity from(RefreshToken refreshToken) {
		return new RefreshTokenJpaEntity(
				refreshToken.getId(),
				refreshToken.getUserId(),
				refreshToken.getTokenHash(),
				refreshToken.getExpiresAt().toEpochMilli(),
				refreshToken.isUsed());
	}

	RefreshToken toDomain() {
		return RefreshToken.of(id, userId, tokenHash, Instant.ofEpochMilli(expiresAt), used);
	}
}
