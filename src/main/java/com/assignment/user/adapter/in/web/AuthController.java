package com.assignment.user.adapter.in.web;

import com.assignment.user.application.port.in.AuthUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class AuthController {

	private final AuthUseCase authUseCase;

	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return new TokenResponse(authUseCase.login(request.username(), request.password()));
	}
}
