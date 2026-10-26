package com.assignment.user.application.service;

import com.assignment.user.application.InvalidCredentialsException;
import com.assignment.user.application.dto.LoginCommand;
import com.assignment.user.application.dto.TokenResult;
import com.assignment.user.application.port.in.AuthUseCase;
import com.assignment.user.application.port.out.AccessTokenPort;
import com.assignment.user.application.port.out.PasswordHashPort;
import com.assignment.user.application.port.out.UserQueryPort;
import com.assignment.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

	private final UserQueryPort userQueryPort;
	private final PasswordHashPort passwordHashPort;
	private final AccessTokenPort accessTokenPort;

	@Override
	@Transactional(readOnly = true)
	public TokenResult login(LoginCommand command) {
		User user = userQueryPort.findByUsername(command.username())
				.filter(found -> passwordHashPort.matches(command.rawPassword(), found.getPassword()))
				.orElseThrow(InvalidCredentialsException::new);
		return new TokenResult(accessTokenPort.issue(user.getId()));
	}
}
