package com.assignment.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	// 테스트 준비용: 고유 username으로 가입·로그인하고 액세스 토큰을 돌려준다(필수 흐름처럼 access_token만 쓴다)
	protected String signupAndLogin() throws Exception {
		String credentials = """
				{"username": "user-%s", "password": "password1234"}
				""".formatted(UUID.randomUUID());
		mockMvc.perform(post("/users/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isCreated());
		String loginResponse = mockMvc.perform(post("/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(loginResponse, "$.access_token");
	}
}
