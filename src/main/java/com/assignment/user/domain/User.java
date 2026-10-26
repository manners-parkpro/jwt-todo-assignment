package com.assignment.user.domain;

import lombok.Getter;

@Getter
public class User {

	private final Long id;
	private String username;
	// 인코딩된 비밀번호(해시)만 담는다. 평문은 도메인에 들어오지 않는다
	private String password;

	private User(Long id, String username, String password) {
		validateUsername(username);
		validatePassword(password);
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

	public void changeUsername(String username) {
		validateUsername(username);
		this.username = username;
	}

	public void changePassword(String encodedPassword) {
		validatePassword(encodedPassword);
		this.password = encodedPassword;
	}

	private static void validateUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username은 비어 있을 수 없습니다.");
		}
	}

	private static void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("비밀번호 해시는 비어 있을 수 없습니다.");
		}
	}
}
