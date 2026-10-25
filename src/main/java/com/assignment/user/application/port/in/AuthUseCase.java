package com.assignment.user.application.port.in;

public interface AuthUseCase {

	/** 자격 증명이 맞으면 액세스 토큰을 발급한다. */
	String login(String username, String rawPassword);
}
