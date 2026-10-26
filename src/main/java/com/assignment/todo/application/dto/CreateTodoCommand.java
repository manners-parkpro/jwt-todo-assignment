package com.assignment.todo.application.dto;

public record CreateTodoCommand(Long userId, String title, String description) {
}
