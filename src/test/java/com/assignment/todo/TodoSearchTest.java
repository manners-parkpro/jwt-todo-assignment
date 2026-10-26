package com.assignment.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class TodoSearchTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[OPT-01] 내 TODO 중 제목에 검색어가 들어간 것만 id 오름차순으로 돌려준다")
	void searchesOwnTodosByTitle() throws Exception {
		String owner = "Bearer " + signupAndLogin();
		String other = "Bearer " + signupAndLogin();
		long weekend = createTodo(owner, "주말 장보기", null);
		createTodo(owner, "운동", "장보기 뒤에 운동");
		long cleanup = createTodo(owner, "장보기 목록 정리", null);
		long report = createTodo(owner, "Weekly REPORT", null);
		createTodo(other, "장보기", null);

		// 설명에만 있는 TODO와 다른 사용자의 TODO는 나오지 않는다
		search(owner, "장보기")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(weekend))
				.andExpect(jsonPath("$[1].id").value(cleanup))
				.andExpect(jsonPath("$[1].title").value("장보기 목록 정리"));
		search(owner, "report")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(report));
		search(owner, "없는 제목")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("[OPT-01] %와 _는 와일드카드가 아니라 문자 그대로 찾는다")
	void treatsLikeWildcardsAsLiterals() throws Exception {
		String owner = "Bearer " + signupAndLogin();
		long percent = createTodo(owner, "100% 달성", null);
		createTodo(owner, "1000 달성", null);
		long underscore = createTodo(owner, "a_b", null);
		createTodo(owner, "axb", null);

		search(owner, "%")
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(percent));
		search(owner, "_")
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(underscore));
	}

	@Test
	@DisplayName("[OPT-01] 검색어가 없거나 공백이거나 100자를 넘으면 400")
	void rejectsInvalidKeyword() throws Exception {
		String owner = "Bearer " + signupAndLogin();

		mockMvc.perform(get("/todos/search").header(HttpHeaders.AUTHORIZATION, owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("keyword"))
				.andExpect(jsonPath("$.errors[0].message").value("검색어는 필수입니다."));
		search(owner, "   ")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("keyword"));
		search(owner, "가".repeat(101))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("검색어는 100자 이하여야 합니다."));
	}

	@Test
	@DisplayName("[OPT-01] 토큰 없이 검색하면 401")
	void searchWithoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(get("/todos/search").param("keyword", "장보기"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
	}

	private ResultActions search(String authorization, String keyword) throws Exception {
		return mockMvc.perform(get("/todos/search")
				.header(HttpHeaders.AUTHORIZATION, authorization)
				.param("keyword", keyword));
	}

	private long createTodo(String authorization, String title, String description) throws Exception {
		String created = mockMvc.perform(post("/todos")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "%s", "description": %s}
								""".formatted(title, description == null ? "null" : "\"" + description + "\"")))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(created, "$.id")).longValue();
	}
}
