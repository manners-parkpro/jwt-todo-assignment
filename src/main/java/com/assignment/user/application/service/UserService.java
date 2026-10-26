package com.assignment.user.application.service;

import com.assignment.user.application.DuplicateUsernameException;
import com.assignment.user.application.InvalidCurrentPasswordException;
import com.assignment.user.application.UserNotFoundException;
import com.assignment.user.application.dto.SignupCommand;
import com.assignment.user.application.dto.UpdateUserCommand;
import com.assignment.user.application.dto.UserResult;
import com.assignment.user.application.port.in.UserUseCase;
import com.assignment.user.application.port.out.PasswordHashPort;
import com.assignment.user.application.port.out.RefreshTokenCommandPort;
import com.assignment.user.application.port.out.UserCommandPort;
import com.assignment.user.application.port.out.UserEventPort;
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
	private final RefreshTokenCommandPort refreshTokenCommandPort;
	private final UserEventPort userEventPort;

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
		return UserResult.from(findUser(userId));
	}

	@Override
	@Transactional
	public UserResult updateMe(UpdateUserCommand command) {
		User user = findUser(command.userId());
		// 현재 비밀번호를 먼저 확인한다 — 그 전에 username 중복을 알려 주면 액세스 토큰만으로 가입된 이름을 떠볼 수 있다
		if (!passwordHashPort.matches(command.currentRawPassword(), user.getPassword())) {
			throw new InvalidCurrentPasswordException();
		}
		if (!user.getUsername().equals(command.username()) && userQueryPort.existsByUsername(command.username())) {
			throw new DuplicateUsernameException();
		}
		user.changeUsername(command.username());
		if (command.newRawPassword() != null) {
			user.changePassword(passwordHashPort.hash(command.newRawPassword()));
			// 다른 기기의 세션도 끝낸다. 이미 발급된 액세스 토큰은 만료(최대 1시간)까지 유효하다(무상태 JWT)
			refreshTokenCommandPort.deleteAllByUserId(user.getId());
		}
		return UserResult.from(userCommandPort.save(user));
	}

	// 행은 남기고 탈퇴로 표시한다(ID 재사용 방지 — User 참고). 리프레시 토큰과 TODO는 같은 트랜잭션에서 지운다
	@Override
	@Transactional
	public void withdraw(Long userId) {
		User user = findUser(userId);
		user.withdraw();
		userCommandPort.save(user);
		refreshTokenCommandPort.deleteAllByUserId(userId);
		// TODO는 todo 기능이 이 이벤트를 받아 지운다(기능 간 직접 참조 금지)
		userEventPort.publishWithdrawn(userId);
	}

	private User findUser(Long userId) {
		return userQueryPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
	}
}
