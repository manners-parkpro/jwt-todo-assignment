package com.assignment.todo.adapter.in.web;

import com.assignment.todo.application.dto.UpdateTodoCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// PUT은 전체 교체라 completed도 필수다. description을 빼면 설명이 지워진다
record UpdateTodoRequest(
		@NotBlank(message = "제목은 필수입니다.")
		@Size(max = 100, message = "제목은 100자 이하여야 합니다.")
		String title,

		@Size(max = 1000, message = "설명은 1,000자 이하여야 합니다.")
		String description,

		@NotNull(message = "완료 여부는 필수입니다.")
		Boolean completed) {

	UpdateTodoCommand toCommand(Long userId, Long todoId) {
		return new UpdateTodoCommand(userId, todoId, title, description, completed);
	}
}
