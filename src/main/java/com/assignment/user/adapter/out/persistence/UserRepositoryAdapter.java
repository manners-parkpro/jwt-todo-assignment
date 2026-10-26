package com.assignment.user.adapter.out.persistence;

import com.assignment.user.application.port.out.UserCommandPort;
import com.assignment.user.application.port.out.UserQueryPort;
import com.assignment.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserCommandPort, UserQueryPort {

	private final UserRepository userRepository;

	@Override
	public User save(User user) {
		return userRepository.save(UserJpaEntity.from(user)).toDomain();
	}

	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id).map(UserJpaEntity::toDomain);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username).map(UserJpaEntity::toDomain);
	}

	@Override
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}
}
