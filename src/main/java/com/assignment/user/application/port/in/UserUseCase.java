package com.assignment.user.application.port.in;

import com.assignment.user.domain.User;

public interface UserUseCase {

	User signup(String username, String rawPassword);

	User getMe(Long userId);
}
