package com.assignment.todo.application.dto;

import com.assignment.todo.domain.Todo;

// 소유자 ID는 담지 않는다 — 유스케이스 밖으로 내보낼 값만 둔다
public record TodoResult(Long id, String title, String description, boolean completed) {

	public static TodoResult from(Todo todo) {
		return new TodoResult(todo.getId(), todo.getTitle(), todo.getDescription(), todo.isCompleted());
	}
}
