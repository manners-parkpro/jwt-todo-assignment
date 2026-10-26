package com.assignment.user.adapter.in.web;

import com.assignment.user.application.port.in.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "사용자")
@RequiredArgsConstructor
class AuthController {

	private final AuthUseCase authUseCase;

	@PostMapping("/login")
	@Operation(summary = "로그인, access_token 반환", description = "재발급용 refresh_token도 함께 돌려준다")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return TokenResponse.from(authUseCase.login(request.toCommand()));
	}

	@PostMapping("/refresh")
	@Operation(summary = "토큰 재발급", description = "refresh_token으로 새 토큰 쌍을 받는다. 재발급할 때마다 refresh_token이 바뀌고, 이미 쓴 토큰을 다시 보내면 그 사용자의 refresh_token이 모두 폐기된다")
	TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return TokenResponse.from(authUseCase.refresh(request.toCommand()));
	}
}
