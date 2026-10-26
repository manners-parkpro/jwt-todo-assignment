package com.assignment.user.application.port.out;

import com.assignment.user.domain.RefreshToken;
import java.util.Optional;

public interface RefreshTokenQueryPort {

	Optional<RefreshToken> findByTokenHash(String tokenHash);
}
