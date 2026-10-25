package com.assignment.user.application.service;

import com.assignment.user.application.InvalidCredentialsException;
import com.assignment.user.application.port.in.AuthUseCase;
import com.assignment.user.application.port.out.PasswordHasher;
import com.assignment.user.application.port.out.TokenIssuer;
import com.assignment.user.application.port.out.UserRepository;
import com.assignment.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;
	private final TokenIssuer tokenIssuer;

	@Override
	@Transactional(readOnly = true)
	public String login(String username, String rawPassword) {
		User user = userRepository.findByUsername(username)
				.filter(found -> passwordHasher.matches(rawPassword, found.getPassword()))
				.orElseThrow(InvalidCredentialsException::new);
		return tokenIssuer.issueAccessToken(user.getId());
	}
}
