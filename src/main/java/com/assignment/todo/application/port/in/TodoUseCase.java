package com.assignment.todo.application.port.in;

import com.assignment.todo.application.dto.CreateTodoCommand;
import com.assignment.todo.application.dto.TodoResult;
import com.assignment.todo.application.dto.UpdateTodoCommand;
import java.util.List;

public interface TodoUseCase {

	TodoResult create(CreateTodoCommand command);

	List<TodoResult> getAll(Long userId);

	TodoResult get(Long userId, Long todoId);

	TodoResult update(UpdateTodoCommand command);

	void delete(Long userId, Long todoId);
}
