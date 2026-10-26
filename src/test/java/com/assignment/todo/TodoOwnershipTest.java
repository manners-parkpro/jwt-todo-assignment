package com.assignment.todo;

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

class TodoOwnershipTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-X2] 다른 사용자의 TODO는 목록에 없고, 조회·수정·삭제하면 404")
	void otherUsersTodoIsHidden() throws Exception {
		String owner = "Bearer " + signupAndLogin();
		String other = "Bearer " + signupAndLogin();
		String created = mockMvc.perform(post("/todos")
						.header(HttpHeaders.AUTHORIZATION, owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "내 할 일"}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		long todoId = ((Number) JsonPath.read(created, "$.id")).longValue();

		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, other))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
		// 없는 TODO와 같은 404·메시지라 다른 사용자의 TODO가 있는지 알 수 없다
		mockMvc.perform(get("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, other))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("TODO를 찾을 수 없습니다."));
		mockMvc.perform(put("/todos/{id}", todoId)
						.header(HttpHeaders.AUTHORIZATION, other)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "남의 할 일 수정", "description": null, "completed": true}
								"""))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, other))
				.andExpect(status().isNotFound());

		// 소유자의 TODO는 바뀌지 않았다
		mockMvc.perform(get("/todos/{id}", todoId).header(HttpHeaders.AUTHORIZATION, owner))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("내 할 일"))
				.andExpect(jsonPath("$.completed").value(false));
	}
}
