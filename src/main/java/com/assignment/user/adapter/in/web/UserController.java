package com.assignment.user.adapter.in.web;

import com.assignment.global.config.OpenApiConfig;
import com.assignment.global.security.CurrentUserId;
import com.assignment.user.application.port.in.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사용자")
@RequiredArgsConstructor
class UserController {

	private final UserUseCase userUseCase;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "회원가입")
	UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return UserResponse.from(userUseCase.signup(request.toCommand()));
	}

	@GetMapping("/me")
	@Operation(summary = "내 정보 조회")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	UserResponse me(@CurrentUserId Long userId) {
		return UserResponse.from(userUseCase.getMe(userId));
	}
}
