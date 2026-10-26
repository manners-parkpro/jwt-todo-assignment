package com.assignment.todo.adapter.in.event;

import com.assignment.global.event.UserWithdrawnEvent;
import com.assignment.todo.application.port.in.TodoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 탈퇴한 사용자의 TODO를 지운다. 동기 리스너라 탈퇴와 같은 트랜잭션(같은 커넥션)에서 실행되고,
// 커밋 뒤에 실행하는 @TransactionalEventListener(AFTER_COMMIT)와 달리 실패하면 탈퇴까지 롤백된다
@Component
@RequiredArgsConstructor
class UserWithdrawnEventListener {

	private final TodoUseCase todoUseCase;

	@EventListener
	void deleteTodosOfWithdrawnUser(UserWithdrawnEvent event) {
		todoUseCase.deleteAll(event.userId());
	}
}
