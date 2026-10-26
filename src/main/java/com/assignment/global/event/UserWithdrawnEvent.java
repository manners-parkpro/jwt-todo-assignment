package com.assignment.global.event;

// 회원 탈퇴를 다른 기능(todo)에 알린다. 기능끼리 직접 참조하지 않도록 이벤트 계약만 global에 둔다
public record UserWithdrawnEvent(Long userId) {
}
