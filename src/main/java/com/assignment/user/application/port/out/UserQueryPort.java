package com.assignment.user.application.port.out;

import com.assignment.user.domain.User;
import java.util.Optional;

public interface UserQueryPort {

	// 탈퇴하지 않은 사용자만 찾는다(로그인·내 정보·수정·탈퇴)
	Optional<User> findActiveById(Long id);

	Optional<User> findActiveByUsername(String username);

	// 탈퇴한 사용자도 포함한다 — 탈퇴자의 username으로는 다시 가입하지 못한다
	boolean existsByUsername(String username);
}
