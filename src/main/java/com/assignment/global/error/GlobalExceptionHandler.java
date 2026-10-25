package com.assignment.global.error;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 예외를 ProblemDetail(RFC 9457) 응답으로 바꾸는 유일한 곳이다. Spring MVC 표준 예외(검증 실패, 읽을 수 없는 본문,
// 경로 변수 타입 오류, 없는 경로 등)는 상위 클래스가 처리하고, 기능 예외의 매핑은 해당 기능을 구현할 때 추가한다.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
}
