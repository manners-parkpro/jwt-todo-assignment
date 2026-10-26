package com.assignment.todo.adapter.in.web;

import com.assignment.global.security.CurrentUserId;
import com.assignment.todo.application.port.in.TodoUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
class TodoController {

	private final TodoUseCase todoUseCase;

	@PostMapping
	ResponseEntity<TodoResponse> create(@CurrentUserId Long userId, @Valid @RequestBody CreateTodoRequest request) {
		TodoResponse response = TodoResponse.from(todoUseCase.create(request.toCommand(userId)));
		// 요청의 Host 헤더에 의존하지 않도록 상대 경로로 둔다(RFC 9110 §10.2.2 허용)
		return ResponseEntity.created(URI.create("/todos/" + response.id())).body(response);
	}

	@GetMapping
	List<TodoResponse> getAll(@CurrentUserId Long userId) {
		return todoUseCase.getAll(userId).stream()
				.map(TodoResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	TodoResponse get(@CurrentUserId Long userId, @PathVariable Long id) {
		return TodoResponse.from(todoUseCase.get(userId, id));
	}

	@PutMapping("/{id}")
	TodoResponse update(@CurrentUserId Long userId, @PathVariable Long id,
			@Valid @RequestBody UpdateTodoRequest request) {
		return TodoResponse.from(todoUseCase.update(request.toCommand(userId, id)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@CurrentUserId Long userId, @PathVariable Long id) {
		todoUseCase.delete(userId, id);
	}
}
