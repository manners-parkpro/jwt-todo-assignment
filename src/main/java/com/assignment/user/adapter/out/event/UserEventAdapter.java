package com.assignment.user.adapter.out.event;

import com.assignment.global.event.UserWithdrawnEvent;
import com.assignment.user.application.port.out.UserEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserEventAdapter implements UserEventPort {

	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void publishWithdrawn(Long userId) {
		eventPublisher.publishEvent(new UserWithdrawnEvent(userId));
	}
}
