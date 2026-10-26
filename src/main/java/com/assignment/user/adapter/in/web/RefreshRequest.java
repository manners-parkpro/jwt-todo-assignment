package com.assignment.user.adapter.in.web;

import com.assignment.user.application.dto.RefreshCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

record RefreshRequest(
		@JsonProperty("refresh_token")
		@NotBlank(message = "refresh_token은 필수입니다.")
		String refreshToken) {

	RefreshCommand toCommand() {
		return new RefreshCommand(refreshToken);
	}
}
