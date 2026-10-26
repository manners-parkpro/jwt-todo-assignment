package com.assignment.user.application;

public class InvalidCurrentPasswordException extends RuntimeException {

	public InvalidCurrentPasswordException() {
		super("현재 비밀번호가 일치하지 않습니다.");
	}
}
