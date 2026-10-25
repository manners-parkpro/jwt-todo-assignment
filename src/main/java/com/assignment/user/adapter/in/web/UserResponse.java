package com.assignment.user.adapter.in.web;

import com.assignment.user.domain.User;

record UserResponse(Long id, String username) {

	static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getUsername());
	}
}
