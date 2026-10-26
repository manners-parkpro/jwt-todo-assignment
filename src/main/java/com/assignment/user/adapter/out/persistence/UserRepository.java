package com.assignment.user.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findByIdAndWithdrawnFalse(Long id);

	Optional<UserJpaEntity> findByUsernameAndWithdrawnFalse(String username);

	boolean existsByUsername(String username);
}
