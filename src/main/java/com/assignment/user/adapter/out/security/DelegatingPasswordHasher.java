package com.assignment.user.adapter.out.security;

import com.assignment.user.application.port.out.PasswordHasher;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class DelegatingPasswordHasher implements PasswordHasher {

	// 기본은 bcrypt이고 저장값에 {bcrypt} 접두사가 붙는다 — 알고리즘을 바꿔도 기존 해시를 검증할 수 있다
	private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String hashedPassword) {
		return passwordEncoder.matches(rawPassword, hashedPassword);
	}
}
