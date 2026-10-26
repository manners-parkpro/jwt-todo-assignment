package com.assignment.user.adapter.in.web;

import com.assignment.global.security.CurrentUserId;
import com.assignment.user.application.port.in.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

	private final UserUseCase userUseCase;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return UserResponse.from(userUseCase.signup(request.toCommand()));
	}

	@GetMapping("/me")
	UserResponse me(@CurrentUserId Long userId) {
		return UserResponse.from(userUseCase.getMe(userId));
	}
}
