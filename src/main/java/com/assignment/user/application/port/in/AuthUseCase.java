package com.assignment.user.application.port.in;

import com.assignment.user.application.dto.LoginCommand;
import com.assignment.user.application.dto.RefreshCommand;
import com.assignment.user.application.dto.TokenResult;

public interface AuthUseCase {

	/** 자격 증명이 맞으면 액세스 토큰과 리프레시 토큰을 발급한다. */
	TokenResult login(LoginCommand command);

	/** 유효한 리프레시 토큰을 새 토큰 쌍으로 바꾼다. 쓴 토큰은 다시 쓸 수 없다. */
	TokenResult refresh(RefreshCommand command);
}
