package com.assignment.todo.domain;

import lombok.Getter;

@Getter
public class Todo {

	private final Long id;
	// 소유자는 사용자 집합체를 직접 참조하지 않고 ID로만 가리킨다
	private final Long userId;
	private String title;
	private String description;
	private boolean completed;

	private Todo(Long id, Long userId, String title, String description, boolean completed) {
		if (userId == null) {
			throw new IllegalArgumentException("TODO의 사용자 ID는 비어 있을 수 없습니다.");
		}
		validateTitle(title);
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.completed = completed;
	}

	public static Todo create(Long userId, String title, String description) {
		return new Todo(null, userId, title, description, false);
	}

	public static Todo of(Long id, Long userId, String title, String description, boolean completed) {
		return new Todo(id, userId, title, description, completed);
	}

	// PUT은 전체 교체라 세 값을 한 번에 바꾼다
	public void update(String title, String description, boolean completed) {
		validateTitle(title);
		this.title = title;
		this.description = description;
		this.completed = completed;
	}

	private static void validateTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("TODO 제목은 비어 있을 수 없습니다.");
		}
	}
}
