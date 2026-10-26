package com.assignment.todo.adapter.out.persistence;

import com.assignment.todo.domain.Todo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// SQLite에서는 FK가 생성되지 않으므로 사용자는 ID 컬럼으로만 참조하고, 소유자 조건 조회를 위해 인덱스를 둔다
@Entity
@Table(name = "todos", indexes = @Index(name = "idx_todos_user_id", columnList = "userId"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TodoJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private boolean completed;

	private TodoJpaEntity(Long id, Long userId, String title, String description, boolean completed) {
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.completed = completed;
	}

	static TodoJpaEntity from(Todo todo) {
		return new TodoJpaEntity(
				todo.getId(),
				todo.getUserId(),
				todo.getTitle(),
				todo.getDescription(),
				todo.isCompleted());
	}

	Todo toDomain() {
		return Todo.of(id, userId, title, description, completed);
	}
}
