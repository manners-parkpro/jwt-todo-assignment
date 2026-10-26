package com.assignment.user;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class UserUpdateTest extends IntegrationTestSupport {

	private static final String PASSWORD = "password1234";

	@Test
	@DisplayName("[OPT-02] username·비밀번호를 바꾸면 새 정보로만 로그인되고, 기존 refresh_token은 폐기된다")
	void changesUsernameAndPassword() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);
		long userId = myId(tokens.accessToken());
		String renamed = "renamed-" + UUID.randomUUID();

		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "%s", "new_password": "newpassword5678"}
				""".formatted(renamed, PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.username").value(renamed));

		login(renamed, "newpassword5678").andExpect(status().isOk());
		login(renamed, PASSWORD).andExpect(status().isUnauthorized());
		login(username, "newpassword5678").andExpect(status().isUnauthorized());
		// 비밀번호를 바꾸면 다른 기기의 세션도 끝난다
		refresh(tokens.refreshToken()).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("[OPT-02] new_password 없이 보내면 비밀번호와 refresh_token은 그대로이고, 자기 username은 중복이 아니다")
	void keepsPasswordWhenNewPasswordIsOmitted() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);

		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "%s"}
				""".formatted(username, PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(username));

		login(username, PASSWORD).andExpect(status().isOk());
		refresh(tokens.refreshToken()).andExpect(status().isOk());
	}

	@Test
	@DisplayName("[OPT-02] 현재 비밀번호가 틀리면 username 중복보다 먼저 400으로 응답하고 아무것도 바꾸지 않는다")
	void rejectsWrongCurrentPasswordBeforeDuplicateCheck() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);
		String taken = "taken-" + UUID.randomUUID();
		signupAndLoginAs(taken, PASSWORD);

		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "wrong-password", "new_password": "newpassword5678"}
				""".formatted(taken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("현재 비밀번호가 일치하지 않습니다."));

		login(username, PASSWORD).andExpect(status().isOk());
		refresh(tokens.refreshToken()).andExpect(status().isOk());
	}

	@Test
	@DisplayName("[OPT-02] 다른 사용자가 쓰는 username으로 바꾸면 409")
	void rejectsUsernameOfAnotherUser() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);
		String taken = "taken-" + UUID.randomUUID();
		signupAndLoginAs(taken, PASSWORD);

		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "%s"}
				""".formatted(taken, PASSWORD))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("이미 사용 중인 username입니다."));
	}

	@Test
	@DisplayName("[OPT-02] 새 비밀번호가 짧거나 공백뿐이거나 현재 비밀번호가 없으면 400")
	void rejectsInvalidRequest() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);

		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "%s", "new_password": "short"}
				""".formatted(username, PASSWORD))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("new_password"))
				.andExpect(jsonPath("$.errors[0].message").value("새 비밀번호는 8~64자여야 합니다."));
		updateMe(tokens.accessToken(), """
				{"username": "%s", "current_password": "%s", "new_password": "          "}
				""".formatted(username, PASSWORD))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[*].message").value(hasItem("새 비밀번호는 공백만으로 이뤄질 수 없습니다.")));
		updateMe(tokens.accessToken(), """
				{"username": "%s"}
				""".formatted(username))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("current_password"));

		login(username, PASSWORD).andExpect(status().isOk());
	}

	@Test
	@DisplayName("[OPT-02] 토큰 없이 수정하면 401")
	void updateWithoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(put("/users/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "anyone", "current_password": "password1234"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
	}

	private ResultActions updateMe(String accessToken, String body) throws Exception {
		return mockMvc.perform(put("/users/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}
}
