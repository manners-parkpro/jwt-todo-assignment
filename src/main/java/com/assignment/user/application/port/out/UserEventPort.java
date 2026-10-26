package com.assignment.user.application.port.out;

public interface UserEventPort {

	// 받는 쪽은 같은 트랜잭션에서 동기로 처리한다 — 실패하면 탈퇴도 함께 롤백된다
	void publishWithdrawn(Long userId);
}
