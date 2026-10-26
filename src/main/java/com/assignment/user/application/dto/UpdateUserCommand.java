package com.assignment.user.application.dto;

// newRawPassword가 null이면 비밀번호를 바꾸지 않는다
public record UpdateUserCommand(Long userId, String username, String currentRawPassword, String newRawPassword) {
}
