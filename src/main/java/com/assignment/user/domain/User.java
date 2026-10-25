package com.assignment.user.domain;

import lombok.Getter;

@Getter
public class User {

	private final Long id;
	private final String username;
	// 인코딩된 비밀번호(해시)만 담는다. 평문은 도메인에 들어오지 않는다
	private final String password;

	private User(Long id, String username, String password) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username은 비어 있을 수 없습니다.");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("비밀번호 해시는 비어 있을 수 없습니다.");
		}
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public static User create(String username, String encodedPassword) {
		return new User(null, username, encodedPassword);
	}

	public static User of(Long id, String username, String encodedPassword) {
		return new User(id, username, encodedPassword);
	}
}
