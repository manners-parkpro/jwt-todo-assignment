package com.assignment.user.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record SignupRequest(
		@NotBlank(message = "username은 필수입니다.")
		@Size(max = 50, message = "username은 50자 이하여야 합니다.")
		String username,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
		@MaxUtf8Bytes(value = 72, message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
		String password) {
}
