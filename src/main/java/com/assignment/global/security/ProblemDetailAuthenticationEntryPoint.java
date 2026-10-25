package com.assignment.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

	// 상태 코드와 WWW-Authenticate 헤더(토큰 오류 정보 포함)는 표준 구현에 맡기고 본문만 채운다
	private final BearerTokenAuthenticationEntryPoint bearerTokenEntryPoint = new BearerTokenAuthenticationEntryPoint();
	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		bearerTokenEntryPoint.commence(request, response, authException);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatusCode.valueOf(response.getStatus()), "유효한 액세스 토큰이 필요합니다.");
		problem.setInstance(URI.create(request.getRequestURI()));
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getOutputStream(), problem);
	}
}
