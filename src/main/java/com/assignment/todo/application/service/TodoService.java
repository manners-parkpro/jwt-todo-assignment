package com.assignment.todo.application.service;

import com.assignment.todo.application.TodoNotFoundException;
import com.assignment.todo.application.dto.CreateTodoCommand;
import com.assignment.todo.application.dto.TodoResult;
import com.assignment.todo.application.dto.UpdateTodoCommand;
import com.assignment.todo.application.port.in.TodoUseCase;
import com.assignment.todo.application.port.out.TodoCommandPort;
import com.assignment.todo.application.port.out.TodoQueryPort;
import com.assignment.todo.domain.Todo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService implements TodoUseCase {

	private final TodoCommandPort todoCommandPort;
	private final TodoQueryPort todoQueryPort;

	@Override
	@Transactional
	public TodoResult create(CreateTodoCommand command) {
		Todo todo = Todo.create(command.userId(), command.title(), command.description());
		return TodoResult.from(todoCommandPort.save(todo));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TodoResult> getAll(Long userId) {
		return todoQueryPort.findAllByUserId(userId).stream()
				.map(TodoResult::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TodoResult> search(Long userId, String keyword) {
		return todoQueryPort.findAllByUserIdAndTitleContaining(userId, keyword).stream()
				.map(TodoResult::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public TodoResult get(Long userId, Long todoId) {
		return TodoResult.from(findOwnedTodo(userId, todoId));
	}

	@Override
	@Transactional
	public TodoResult update(UpdateTodoCommand command) {
		Todo todo = findOwnedTodo(command.userId(), command.todoId());
		todo.update(command.title(), command.description(), command.completed());
		return TodoResult.from(todoCommandPort.save(todo));
	}

	@Override
	@Transactional
	public void delete(Long userId, Long todoId) {
		todoCommandPort.delete(findOwnedTodo(userId, todoId));
	}

	// 다른 사용자의 TODO도 없는 TODO와 같은 404로 응답해 존재 여부를 드러내지 않는다
	private Todo findOwnedTodo(Long userId, Long todoId) {
		return todoQueryPort.findByIdAndUserId(todoId, userId).orElseThrow(TodoNotFoundException::new);
	}
}
