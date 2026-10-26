package com.assignment.todo.adapter.in.web;

import com.assignment.todo.application.dto.CreateTodoCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateTodoRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,

		@Size(max = 1000, message = "설명은 1,000자 이하여야 합니다.")
		String description) {

	CreateTodoCommand toCommand(Long userId) {
		return new CreateTodoCommand(userId, title, description);
	}
}
