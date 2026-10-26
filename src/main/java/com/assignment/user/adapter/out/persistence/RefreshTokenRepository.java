package com.assignment.user.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

	Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

	void deleteAllByUserId(Long userId);

	void deleteAllByUserIdAndExpiresAtLessThanEqual(Long userId, Long expiresAt);
}
