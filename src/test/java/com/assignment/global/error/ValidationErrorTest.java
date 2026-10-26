package com.assignment.global.error;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ValidationErrorTest extends IntegrationTestSupport {

	static Stream<Arguments> invalidSignups() {
		return Stream.of(
				Arguments.of("", "password1234", "username", "username은 필수입니다."),
				Arguments.of("u".repeat(51), "password1234", "username", "username은 50자 이하여야 합니다."),
				Arguments.of("user-" + UUID.randomUUID(), "short", "password", "비밀번호는 8~64자여야 합니다."),
				// 25자라 글자 수 규칙은 통과하지만 UTF-8로 75바이트라 bcrypt 한도(72바이트)를 넘는다
				Arguments.of("user-" + UUID.randomUUID(), "가".repeat(25), "password", "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."));
	}

	@DisplayName("[TEST-X4] 회원가입 입력 검증 실패 → 400과 필드별 오류")
	@ParameterizedTest(name = "[TEST-X4] {2}: {3}")
	@MethodSource("invalidSignups")
	void signupValidationFailure(String username, String password, String field, String message) throws Exception {
		expectFieldError(mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username": "%s", "password": "%s"}
						""".formatted(username, password))), field, message);
	}

	@Test
	@DisplayName("[TEST-X4] TODO 입력 검증 실패 → 400과 필드별 오류")
	void todoValidationFailure() throws Exception {
		String authorization = "Bearer " + signupAndLogin();

		expectFieldError(createTodo(authorization, """
				{"title": "   "}
				"""), "title", "제목은 필수입니다.");
		expectFieldError(createTodo(authorization, """
				{"title": "%s"}
				""".formatted("가".repeat(101))), "title", "제목은 100자 이하여야 합니다.");
		expectFieldError(createTodo(authorization, """
				{"title": "할 일", "description": "%s"}
				""".formatted("가".repeat(1001))), "description", "설명은 1,000자 이하여야 합니다.");

		String created = createTodo(authorization, """
				{"title": "할 일"}
				""")
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long todoId = ((Number) JsonPath.read(created, "$.id")).longValue();
		// PUT은 전체 교체라 completed를 빼면 400이다
		expectFieldError(mockMvc.perform(put("/todos/{id}", todoId)
				.header(HttpHeaders.AUTHORIZATION, authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "할 일"}
						""")), "completed", "완료 여부는 필수입니다.");
	}

	@Test
	@DisplayName("[TEST-X4] 본문이 올바른 JSON이 아니거나 경로의 ID가 숫자가 아니면 400")
	void unreadableRequestIsBadRequest() throws Exception {
		String authorization = "Bearer " + signupAndLogin();

		createTodo(authorization, "{\"title\": ")
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors").doesNotExist());
		mockMvc.perform(get("/todos/{id}", "abc").header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400));
	}

	private ResultActions createTodo(String authorization, String body) throws Exception {
		return mockMvc.perform(post("/todos")
				.header(HttpHeaders.AUTHORIZATION, authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private static void expectFieldError(ResultActions result, String field, String message) throws Exception {
		result.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("요청 값이 올바르지 않습니다."))
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value(field))
				.andExpect(jsonPath("$.errors[0].message").value(message));
	}
}
