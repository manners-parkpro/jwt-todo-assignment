package com.assignment.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class DuplicateSignupTest extends IntegrationTestSupport {

	@Test
	@DisplayName("[TEST-X3] 이미 가입된 username으로 가입하면 409이고 기존 계정은 바뀌지 않는다")
	void duplicateUsernameIsConflict() throws Exception {
		String username = "user-" + UUID.randomUUID();
		signupAndLoginAs(username, "password1234");

		mockMvc.perform(post("/users/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "%s", "password": "another-password"}
								""".formatted(username)))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("이미 사용 중인 username입니다."));

		// 기존 계정의 비밀번호가 덮어쓰이지 않았다
		login(username, "password1234").andExpect(status().isOk());
		login(username, "another-password").andExpect(status().isUnauthorized());
	}
}
