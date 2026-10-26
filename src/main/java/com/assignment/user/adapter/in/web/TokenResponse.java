package com.assignment.user.adapter.in.web;

import com.assignment.user.application.dto.TokenResult;
import com.fasterxml.jackson.annotation.JsonProperty;

record TokenResponse(@JsonProperty("access_token") String accessToken) {

	static TokenResponse from(TokenResult result) {
		return new TokenResponse(result.accessToken());
	}
}
