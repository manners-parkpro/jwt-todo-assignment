package com.assignment.user.adapter.in.web;

import com.assignment.user.application.dto.TokenResult;
import com.fasterxml.jackson.annotation.JsonProperty;

// access_token은 명세의 필드이고 refresh_token은 재발급 확장 필드다(D-15)
record TokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("refresh_token") String refreshToken) {

	static TokenResponse from(TokenResult result) {
		return new TokenResponse(result.accessToken(), result.refreshToken());
	}
}
