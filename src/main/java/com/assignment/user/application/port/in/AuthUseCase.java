package com.assignment.user.application.port.in;

import com.assignment.user.application.dto.LoginCommand;
import com.assignment.user.application.dto.TokenResult;

public interface AuthUseCase {

	/** 자격 증명이 맞으면 액세스 토큰을 발급한다. */
	TokenResult login(LoginCommand command);
}
