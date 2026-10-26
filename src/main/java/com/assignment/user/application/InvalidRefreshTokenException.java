package com.assignment.user.application;

// 없는 토큰·만료된 토큰·이미 쓴 토큰을 구분하지 않는다(어느 경우인지 알려 주지 않기 위해)
public class InvalidRefreshTokenException extends RuntimeException {

	public InvalidRefreshTokenException() {
		super("리프레시 토큰이 유효하지 않습니다.");
	}
}
