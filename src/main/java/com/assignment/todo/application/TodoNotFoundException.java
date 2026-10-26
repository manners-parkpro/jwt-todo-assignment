package com.assignment.todo.application;

public class TodoNotFoundException extends RuntimeException {

	public TodoNotFoundException() {
		super("TODO를 찾을 수 없습니다.");
	}
}
