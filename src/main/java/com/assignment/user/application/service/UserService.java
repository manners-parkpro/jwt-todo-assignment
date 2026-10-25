package com.assignment.user.application.service;

import com.assignment.user.application.DuplicateUsernameException;
import com.assignment.user.application.UserNotFoundException;
import com.assignment.user.application.port.in.UserUseCase;
import com.assignment.user.application.port.out.PasswordHasher;
import com.assignment.user.application.port.out.UserRepository;
import com.assignment.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	@Override
	@Transactional
	public User signup(String username, String rawPassword) {
		if (userRepository.existsByUsername(username)) {
			throw new DuplicateUsernameException();
		}
		return userRepository.save(User.create(username, passwordHasher.hash(rawPassword)));
	}

	@Override
	@Transactional(readOnly = true)
	public User getMe(Long userId) {
		return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
	}
}
