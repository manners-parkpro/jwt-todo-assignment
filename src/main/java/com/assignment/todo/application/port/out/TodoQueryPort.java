package com.assignment.todo.application.port.out;

import com.assignment.todo.domain.Todo;
import java.util.List;
import java.util.Optional;

public interface TodoQueryPort {

	Optional<Todo> findByIdAndUserId(Long id, Long userId);

	// id 오름차순
	List<Todo> findAllByUserId(Long userId);

	// 제목에 keyword가 들어간 것만 id 오름차순 — 영문 대소문자를 구분하지 않고 %·_는 문자 그대로 찾는다
	List<Todo> findAllByUserIdAndTitleContaining(Long userId, String keyword);
}
