package com.assignment.user.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

// 가입 규칙(길이)은 검사하지 않는다 — 틀린 값은 401로 응답하고, bcrypt 비교는 72바이트를 넘어도 예외 없이 동작한다
record LoginRequest(
		@NotBlank(message = "username은 필수입니다.")
		String username,

		@NotBlank(message = "비밀번호는 필수입니다.")
		String password) {
}
