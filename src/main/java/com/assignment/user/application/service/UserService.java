package com.assignment.user.application.service;

import com.assignment.user.application.DuplicateUsernameException;
import com.assignment.user.application.UserNotFoundException;
import com.assignment.user.application.dto.SignupCommand;
import com.assignment.user.application.dto.UserResult;
import com.assignment.user.application.port.in.UserUseCase;
import com.assignment.user.application.port.out.PasswordHashPort;
import com.assignment.user.application.port.out.UserCommandPort;
import com.assignment.user.application.port.out.UserQueryPort;
import com.assignment.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

	private final UserCommandPort userCommandPort;
	private final UserQueryPort userQueryPort;
	private final PasswordHashPort passwordHashPort;

	@Override
	@Transactional
	public UserResult signup(SignupCommand command) {
		if (userQueryPort.existsByUsername(command.username())) {
			throw new DuplicateUsernameException();
		}
		User user = User.create(command.username(), passwordHashPort.hash(command.rawPassword()));
		return UserResult.from(userCommandPort.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResult getMe(Long userId) {
		return userQueryPort.findById(userId)
				.map(UserResult::from)
				.orElseThrow(UserNotFoundException::new);
	}
}
