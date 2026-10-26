package com.assignment.todo.adapter.in.web;

import com.assignment.todo.application.dto.TodoResult;

record TodoResponse(Long id, String title, String description, boolean completed) {

	static TodoResponse from(TodoResult result) {
		return new TodoResponse(result.id(), result.title(), result.description(), result.completed());
	}
}
