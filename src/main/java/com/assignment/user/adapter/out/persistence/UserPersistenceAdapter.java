package com.assignment.user.adapter.out.persistence;

import com.assignment.user.application.port.out.UserRepository;
import com.assignment.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(User user) {
		return userJpaRepository.save(UserJpaEntity.from(user)).toDomain();
	}

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id).map(UserJpaEntity::toDomain);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userJpaRepository.findByUsername(username).map(UserJpaEntity::toDomain);
	}

	@Override
	public boolean existsByUsername(String username) {
		return userJpaRepository.existsByUsername(username);
	}
}
