package com.assignment.user.application.dto;

import com.assignment.user.domain.User;

// 비밀번호 해시는 담지 않는다 — 유스케이스 밖으로 내보낼 값만 둔다
public record UserResult(Long id, String username) {

	public static UserResult from(User user) {
		return new UserResult(user.getId(), user.getUsername());
	}
}
