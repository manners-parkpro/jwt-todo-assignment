package com.assignment.user.adapter.in.web;

import com.assignment.user.application.dto.UpdateUserCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// PUT은 전체 교체라 username도 보낸다. new_password는 선택이며, 보내면 가입 비밀번호와 같은 규칙을 따른다
record UpdateUserRequest(
		@NotBlank(message = "username은 필수입니다.")
		@Size(max = 50, message = "username은 50자 이하여야 합니다.")
		String username,

		@JsonProperty("current_password")
		@NotBlank(message = "현재 비밀번호는 필수입니다.")
		String currentPassword,

		// 선택 값이라 @NotBlank 대신, 보냈을 때만 공백뿐인 값을 막는다
		@JsonProperty("new_password")
		@Size(min = 8, max = 64, message = "새 비밀번호는 8~64자여야 합니다.")
		@Pattern(regexp = "(?s).*\\S.*", message = "새 비밀번호는 공백만으로 이뤄질 수 없습니다.")
		@MaxUtf8Bytes(value = 72, message = "새 비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
		String newPassword) {

	UpdateUserCommand toCommand(Long userId) {
		return new UpdateUserCommand(userId, username, currentPassword, newPassword);
	}
}
