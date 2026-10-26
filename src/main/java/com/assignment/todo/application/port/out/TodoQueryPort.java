package com.assignment.todo.application.port.out;

import com.assignment.todo.domain.Todo;
import java.util.List;
import java.util.Optional;

public interface TodoQueryPort {

	Optional<Todo> findByIdAndUserId(Long id, Long userId);

	// id 오름차순
	List<Todo> findAllByUserId(Long userId);
}
