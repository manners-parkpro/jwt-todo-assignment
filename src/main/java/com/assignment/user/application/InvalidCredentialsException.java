package com.assignment.user.application;

// 없는 사용자와 틀린 비밀번호를 구분하지 않는다(계정 존재 여부를 알려 주지 않기 위해)
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("username 또는 비밀번호가 올바르지 않습니다.");
	}
}
