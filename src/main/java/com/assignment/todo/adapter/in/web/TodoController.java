package com.assignment.todo.adapter.in.web;

import com.assignment.global.config.OpenApiConfig;
import com.assignment.global.security.CurrentUserId;
import com.assignment.todo.application.port.in.TodoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "TODO")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RequiredArgsConstructor
class TodoController {

	private final TodoUseCase todoUseCase;

	@PostMapping
	@Operation(summary = "생성")
	// 문서 생성기는 ResponseEntity가 정하는 상태 코드를 알 수 없어 200으로 표시하므로 201을 명시한다
	@ApiResponse(responseCode = "201", description = "생성됨 — Location 헤더에 새 TODO 경로(/todos/{id})")
	ResponseEntity<TodoResponse> create(@CurrentUserId Long userId, @Valid @RequestBody CreateTodoRequest request) {
		TodoResponse response = TodoResponse.from(todoUseCase.create(request.toCommand(userId)));
		// 요청의 Host 헤더에 의존하지 않도록 상대 경로로 둔다(RFC 9110 §10.2.2 허용)
		return ResponseEntity.created(URI.create("/todos/" + response.id())).body(response);
	}

	@GetMapping
	@Operation(summary = "내 목록 조회", description = "id 오름차순으로 돌려준다")
	List<TodoResponse> getAll(@CurrentUserId Long userId) {
		return todoUseCase.getAll(userId).stream()
				.map(TodoResponse::from)
				.toList();
	}

	@GetMapping("/search")
	@Operation(summary = "제목으로 검색", description = "내 TODO 중 제목에 검색어가 들어간 것을 id 오름차순으로 돌려준다. 영문 대소문자를 구분하지 않고, %와 _는 문자 그대로 찾는다")
	List<TodoResponse> search(@CurrentUserId Long userId, @Valid @ParameterObject TodoSearchRequest request) {
		return todoUseCase.search(userId, request.keyword()).stream()
				.map(TodoResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "단건 조회")
	TodoResponse get(@CurrentUserId Long userId, @PathVariable Long id) {
		return TodoResponse.from(todoUseCase.get(userId, id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "수정", description = "전체 교체라 completed도 필수다. description을 빼면 설명이 지워진다")
	TodoResponse update(@CurrentUserId Long userId, @PathVariable Long id,
			@Valid @RequestBody UpdateTodoRequest request) {
		return TodoResponse.from(todoUseCase.update(request.toCommand(userId, id)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "삭제")
	void delete(@CurrentUserId Long userId, @PathVariable Long id) {
		todoUseCase.delete(userId, id);
	}
}
