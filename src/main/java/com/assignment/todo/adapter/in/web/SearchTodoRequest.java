package com.assignment.todo.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 쿼리 파라미터(?keyword=)를 레코드로 받아, 검증 실패도 다른 요청과 같은 errors 형식으로 응답한다
record SearchTodoRequest(
		@NotBlank(message = "검색어는 필수입니다.")
		@Size(max = 100, message = "검색어는 100자 이하여야 합니다.")
		String keyword) {
}
