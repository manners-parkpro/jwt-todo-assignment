package com.assignment.user.application.port.out;

public interface TokenIssuer {

	String issueAccessToken(Long userId);
}
