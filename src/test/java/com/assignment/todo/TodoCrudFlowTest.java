package com.assignment.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class TodoCrudFlowTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-02] TODO 생성 → 목록 조회 → 수정 → 삭제")
	void createListUpdateDelete() throws Exception {
		String authorization = "Bearer " + signupAndLogin();

		// 생성: 201과 Location, 완료 여부는 false로 시작한다
		MvcResult created = mockMvc.perform(post("/todos")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "장보기", "description": "우유와 계란"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("장보기"))
				.andExpect(jsonPath("$.description").value("우유와 계란"))
				.andExpect(jsonPath("$.completed").value(false))
				.andReturn();
		long todoId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
		assertThat(created.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/todos/" + todoId);

		// 목록: 새로 가입한 사용자라 방금 만든 TODO 하나만 있다
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(todoId))
				.andExpect(jsonPath("$[0].title").value("장보기"));

		// 수정: 전체 교체한 뒤 다시 조회해 저장됐는지 확인한다
		mockMvc.perform(put("/todos/{id}", todoId)
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "장보기 완료", "description": "우유와 계란, 빵", "completed": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(todoId))
				.andExpect(jsonPath("$.title").value("장보기 완료"))
				.andExpect(jsonPath("$.completed").value(true));
		mockMvc.perform(get("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("장보기 완료"))
				.andExpect(jsonPath("$.description").value("우유와 계란, 빵"))
				.andExpect(jsonPath("$.completed").value(true));

		// 삭제: 204 뒤에는 단건 조회가 404이고 목록에서도 빠진다
		mockMvc.perform(delete("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}
}
