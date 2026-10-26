package com.assignment.todo.application.port.out;

import com.assignment.todo.domain.Todo;

public interface TodoCommandPort {

	Todo save(Todo todo);

	void delete(Todo todo);
}
