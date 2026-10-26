package com.assignment.todo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class TodoNotFoundTest extends IntegrationTestSupport {

	// 한 번도 발급되지 않는 ID — SQLite는 삭제한 마지막 ID를 새 행에 다시 쓰므로 삭제한 ID는 쓰지 않는다
	private static final long NON_EXISTENT_ID = Long.MAX_VALUE;

	@DisplayName("[TEST-04] 존재하지 않는 TODO ID로 요청하면 404")
	@ParameterizedTest(name = "[TEST-04] 존재하지 않는 TODO ID로 {0} → 404")
	@ValueSource(strings = {"GET", "PUT", "DELETE"})
	void requestWithNonExistentIdIsNotFound(String method) throws Exception {
		MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method), "/todos/{id}", NON_EXISTENT_ID)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + signupAndLogin());
		if (method.equals("PUT")) {
			// 수정은 본문 검증(400)을 통과해야 404까지 가므로 올바른 본문을 보낸다
			request.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"title": "수정", "description": null, "completed": true}
							""");
		}

		mockMvc.perform(request)
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("TODO를 찾을 수 없습니다."));
	}
}
