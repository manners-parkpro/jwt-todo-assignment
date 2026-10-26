package com.assignment.todo.application.dto;

public record UpdateTodoCommand(Long userId, Long todoId, String title, String description, boolean completed) {
}
