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
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	// 테스트 준비용: 고유 username으로 가입·로그인하고 액세스 토큰만 돌려준다.
	// 필수 테스트가 쓰는 도우미라 refresh_token은 읽지 않는다(확장 기능에 의존하지 않기 위해)
	protected String signupAndLogin() throws Exception {
		return JsonPath.read(signupAndLoginResponse(), "$.access_token");
	}

	// 재발급 확장 테스트용: 액세스 토큰과 리프레시 토큰을 함께 돌려준다
	protected Tokens signupAndLoginWithRefreshToken() throws Exception {
		String loginResponse = signupAndLoginResponse();
		return new Tokens(
				JsonPath.read(loginResponse, "$.access_token"),
				JsonPath.read(loginResponse, "$.refresh_token"));
	}

	protected ResultActions refresh(String refreshToken) throws Exception {
		return mockMvc.perform(post("/users/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"refresh_token": "%s"}
						""".formatted(refreshToken)));
	}

	private String signupAndLoginResponse() throws Exception {
		String credentials = """
				{"username": "user-%s", "password": "password1234"}
				""".formatted(UUID.randomUUID());
		mockMvc.perform(post("/users/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isCreated());
		return mockMvc.perform(post("/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(credentials))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	}

	protected record Tokens(String accessToken, String refreshToken) {
	}
}
