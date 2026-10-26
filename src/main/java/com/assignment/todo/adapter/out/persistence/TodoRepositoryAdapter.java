package com.assignment.todo.adapter.out.persistence;

import com.assignment.todo.application.port.out.TodoCommandPort;
import com.assignment.todo.application.port.out.TodoQueryPort;
import com.assignment.todo.domain.Todo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TodoRepositoryAdapter implements TodoCommandPort, TodoQueryPort {

	private final TodoRepository todoRepository;

	@Override
	public Todo save(Todo todo) {
		return todoRepository.save(TodoJpaEntity.from(todo)).toDomain();
	}

	@Override
	public void delete(Todo todo) {
		todoRepository.deleteById(todo.getId());
	}

	@Override
	public Optional<Todo> findByIdAndUserId(Long id, Long userId) {
		return todoRepository.findByIdAndUserId(id, userId).map(TodoJpaEntity::toDomain);
	}

	@Override
	public List<Todo> findAllByUserId(Long userId) {
		return todoRepository.findAllByUserIdOrderByIdAsc(userId).stream()
				.map(TodoJpaEntity::toDomain)
				.toList();
	}

	@Override
	public List<Todo> findAllByUserIdAndTitleContaining(Long userId, String keyword) {
		return todoRepository.findAllByUserIdAndTitleContainingIgnoreCaseOrderByIdAsc(userId, keyword).stream()
				.map(TodoJpaEntity::toDomain)
				.toList();
	}
}
