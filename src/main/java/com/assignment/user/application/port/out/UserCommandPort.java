package com.assignment.user.application.port.out;

import com.assignment.user.domain.User;

public interface UserCommandPort {

	User save(User user);
}
