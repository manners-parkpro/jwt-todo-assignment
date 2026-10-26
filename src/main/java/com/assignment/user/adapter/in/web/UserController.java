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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	@PutMapping("/me")
	@Operation(summary = "회원정보 수정", description = "current_password를 확인한 뒤 username과 비밀번호(new_password, 선택)를 바꾼다. 비밀번호를 바꾸면 그 사용자의 refresh_token이 모두 폐기된다")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	UserResponse updateMe(@CurrentUserId Long userId, @Valid @RequestBody UpdateUserRequest request) {
		return UserResponse.from(userUseCase.updateMe(request.toCommand(userId)));
	}

	@DeleteMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "탈퇴", description = "내 TODO와 refresh_token을 지우고 계정을 탈퇴로 표시한다. 이후 로그인·재발급은 401이고, 같은 username으로 다시 가입할 수 없다")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	void withdraw(@CurrentUserId Long userId) {
		userUseCase.withdraw(userId);
	}
}
