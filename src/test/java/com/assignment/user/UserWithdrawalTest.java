package com.assignment.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

class UserWithdrawalTest extends IntegrationTestSupport {

	private static final String PASSWORD = "password1234";

	@Test
	@DisplayName("[OPT-03] 탈퇴하면 로그인·재발급은 401이고, 남은 액세스 토큰으로 내 정보·수정·탈퇴를 하면 404")
	void withdrawnAccountCannotBeUsed() throws Exception {
		String username = "user-" + UUID.randomUUID();
		Tokens tokens = signupAndLoginAs(username, PASSWORD);

		withdraw(tokens.accessToken())
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		login(username, PASSWORD)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("username 또는 비밀번호가 올바르지 않습니다."));
		refresh(tokens.refreshToken()).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
				.andExpect(status().isNotFound());
		mockMvc.perform(put("/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "%s", "current_password": "%s"}
								""".formatted(username, PASSWORD)))
				.andExpect(status().isNotFound());
		withdraw(tokens.accessToken()).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("[OPT-03] 탈퇴하면 내 TODO는 지워지고 다른 사용자의 TODO는 그대로다")
	void deletesOnlyTodosOfWithdrawnUser() throws Exception {
		String member = signupAndLoginAs("user-" + UUID.randomUUID(), PASSWORD).accessToken();
		String other = signupAndLoginAs("user-" + UUID.randomUUID(), PASSWORD).accessToken();
		createTodo(member, "탈퇴할 사람의 할 일 1");
		createTodo(member, "탈퇴할 사람의 할 일 2");
		createTodo(other, "남는 사람의 할 일");

		withdraw(member).andExpect(status().isNoContent());

		// 탈퇴 전에 받은 액세스 토큰은 만료 전까지 유효하므로, 그 토큰으로 지워졌는지 확인한다
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, "Bearer " + member))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, "Bearer " + other))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("남는 사람의 할 일"));
	}

	@Test
	@DisplayName("[OPT-03] 새 가입자는 탈퇴한 사용자의 ID를 받지 않고, 탈퇴자의 토큰으로 새 계정에 접근할 수 없다")
	void newUserDoesNotReuseWithdrawnUserId() throws Exception {
		// 가장 최근 가입자가 탈퇴한다 — 행을 지우면 SQLite가 이 ID를 다음 가입자에게 다시 준다
		String withdrawn = signupAndLoginAs("user-" + UUID.randomUUID(), PASSWORD).accessToken();
		long withdrawnId = myId(withdrawn);
		withdraw(withdrawn).andExpect(status().isNoContent());

		String newcomer = signupAndLoginAs("user-" + UUID.randomUUID(), PASSWORD).accessToken();
		createTodo(newcomer, "새 가입자의 할 일");

		assertThat(myId(newcomer)).isGreaterThan(withdrawnId);
		mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + withdrawn))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/todos").header(HttpHeaders.AUTHORIZATION, "Bearer " + withdrawn))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("[OPT-03] 탈퇴한 username으로는 다시 가입할 수 없다(409)")
	void withdrawnUsernameStaysReserved() throws Exception {
		String username = "user-" + UUID.randomUUID();
		withdraw(signupAndLoginAs(username, PASSWORD).accessToken()).andExpect(status().isNoContent());

		mockMvc.perform(post("/users/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username": "%s", "password": "%s"}
								""".formatted(username, PASSWORD)))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("[OPT-03] 토큰 없이 탈퇴하면 401")
	void withdrawWithoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(delete("/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
	}

	private ResultActions withdraw(String accessToken) throws Exception {
		return mockMvc.perform(delete("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
	}

	private void createTodo(String accessToken, String title) throws Exception {
		mockMvc.perform(post("/todos")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "%s"}
								""".formatted(title)))
				.andExpect(status().isCreated());
	}
}
