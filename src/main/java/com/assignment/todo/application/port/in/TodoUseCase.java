package com.assignment.todo.application.port.in;

import com.assignment.todo.application.dto.CreateTodoCommand;
import com.assignment.todo.application.dto.TodoResult;
import com.assignment.todo.application.dto.UpdateTodoCommand;
import java.util.List;

public interface TodoUseCase {

	TodoResult create(CreateTodoCommand command);

	List<TodoResult> getAll(Long userId);

	List<TodoResult> search(Long userId, String keyword);

	TodoResult get(Long userId, Long todoId);

	TodoResult update(UpdateTodoCommand command);

	void delete(Long userId, Long todoId);

	// 회원 탈퇴 때 그 사용자의 TODO를 모두 지운다
	void deleteAll(Long userId);
}
