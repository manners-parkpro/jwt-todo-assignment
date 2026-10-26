package com.assignment.global.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

	private static final RequestMatcher PUBLIC_AUTH_ENDPOINTS = new OrRequestMatcher(
			PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/users/signup"),
			PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/users/login"),
			PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/users/refresh"));

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			ProblemDetailAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
		return http
				// 쿠키·세션 없이 Bearer 토큰만 쓰는 API라 CSRF 보호, 세션, 폼 로그인·Basic 인증·로그아웃 기본 기능이 필요 없다
				// (로그아웃 필터를 남겨 두면 /logout이 인증 없이 302로 응답한다)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						// 오류 디스패치까지 인증을 요구하면 400·500 응답이 401로 바뀐다
						.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
						.requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(Customizer.withDefaults())
						.bearerTokenResolver(bearerTokenResolver())
						.authenticationEntryPoint(authenticationEntryPoint))
				.build();
	}

	// 리소스 서버는 permitAll 경로에서도 헤더의 토큰을 검증하므로, 공개 엔드포인트에서는 헤더를 무시해
	// 만료된 토큰을 그대로 실어 보내는 클라이언트도 다시 로그인하거나 토큰을 재발급받을 수 있게 한다
	private static BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
		return request -> PUBLIC_AUTH_ENDPOINTS.matches(request) ? null : defaultResolver.resolve(request);
	}
}
