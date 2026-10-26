package com.assignment.user.application.port.in;

import com.assignment.user.application.dto.SignupCommand;
import com.assignment.user.application.dto.UserResult;

public interface UserUseCase {

	UserResult signup(SignupCommand command);

	UserResult getMe(Long userId);
}
