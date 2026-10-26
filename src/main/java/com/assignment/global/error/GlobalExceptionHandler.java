package com.assignment.global.error;

import com.assignment.todo.application.TodoNotFoundException;
import com.assignment.user.application.DuplicateUsernameException;
import com.assignment.user.application.InvalidCredentialsException;
import com.assignment.user.application.InvalidRefreshTokenException;
import com.assignment.user.application.UserNotFoundException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 예외를 ProblemDetail(RFC 9457) 응답으로 바꾸는 유일한 곳이다. Spring MVC 표준 예외(검증 실패, 읽을 수 없는 본문,
// 경로 변수 타입 오류, 없는 경로 등)는 상위 클래스가 처리하고, 기능 예외의 매핑은 해당 기능을 구현할 때 추가한다.
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private final ObjectMapper objectMapper;

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ex.getBody();
		problem.setDetail("요청 값이 올바르지 않습니다.");
		problem.setProperty("errors", ex.getFieldErrors().stream()
				.map(error -> new FieldErrorResponse(
						jsonFieldName(ex.getTarget(), error.getField()),
						error.getDefaultMessage()))
				.toList());
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@ExceptionHandler(DuplicateUsernameException.class)
	ResponseEntity<ProblemDetail> handleDuplicateUsername(DuplicateUsernameException e) {
		return problem(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
	ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException e) {
		// 401 응답에는 WWW-Authenticate 헤더가 있어야 한다(RFC 9110 §15.5.2)
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
				.body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage()));
	}

	// 다른 사용자의 TODO도 TodoNotFoundException으로 오므로 응답만으로는 존재 여부를 알 수 없다
	@ExceptionHandler({UserNotFoundException.class, TodoNotFoundException.class})
	ResponseEntity<ProblemDetail> handleNotFound(RuntimeException e) {
		return problem(HttpStatus.NOT_FOUND, e.getMessage());
	}

	private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
		return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
	}

	// 검증 오류는 자바 필드 이름(refreshToken)으로 나오므로, 클라이언트가 보낸 JSON 이름(refresh_token)으로 바꿔 알려 준다
	private String jsonFieldName(Object target, String field) {
		if (target == null) {
			return field;
		}
		BeanDescription description = objectMapper.getDeserializationConfig()
				.introspect(objectMapper.constructType(target.getClass()));
		return description.findProperties().stream()
				.filter(property -> property.getInternalName().equals(field))
				.map(BeanPropertyDefinition::getName)
				.findFirst()
				.orElse(field);
	}

	record FieldErrorResponse(String field, String message) {
	}
}
