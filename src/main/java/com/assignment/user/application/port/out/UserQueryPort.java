package com.assignment.user.application.port.out;

import com.assignment.user.domain.User;
import java.util.Optional;

public interface UserQueryPort {

	Optional<User> findById(Long id);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);
}
