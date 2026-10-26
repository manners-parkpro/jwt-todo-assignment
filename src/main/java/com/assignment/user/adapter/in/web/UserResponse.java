package com.assignment.user.adapter.in.web;

import com.assignment.user.application.dto.UserResult;

record UserResponse(Long id, String username) {

	static UserResponse from(UserResult result) {
		return new UserResponse(result.id(), result.username());
	}
}
