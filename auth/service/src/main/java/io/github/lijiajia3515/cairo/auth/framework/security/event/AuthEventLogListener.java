package io.github.lijiajia3515.cairo.auth.framework.security.event;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureProviderNotFoundEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 认证事件监听类
 */
@Slf4j
@Component
public class AuthEventLogListener {

	/**
	 * 交互式方式认证成功 事件监听
	 *
	 * @param event 事件
	 */
	@NewSpan
	@EventListener(InteractiveAuthenticationSuccessEvent.class)
	public void interactiveAuthenticationSuccessEventListener(InteractiveAuthenticationSuccessEvent event) {
		log.debug("InteractiveAuthenticationSuccessEvent: [ts: {} authentication: {} class: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getGeneratedBy().getName());
	}

	/**
	 * 登出成功 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(LogoutSuccessEvent.class)
	@NewSpan
	public void logoutSuccessEventListener(LogoutSuccessEvent event) {
		log.debug("LogoutSuccessEvent: [ts: {} authentication: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication());
	}

	/**
	 * 认证服务异常 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationFailureServiceExceptionEvent.class)
	@NewSpan
	public void authenticationFailureServiceExceptionEventListener(AuthenticationFailureServiceExceptionEvent event) {
		log.debug("AuthenticationFailureServiceExceptionEvent: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
		log.info("AuthenticationFailureServiceExceptionEvent", event.getException());
	}

	/**
	 * 认证提供方异常 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthenticationFailureProviderNotFoundEvent.class)
	@NewSpan
	public void authenticationFailureProviderNotFoundEvent(AuthenticationFailureProviderNotFoundEvent event) {
		log.trace("AuthenticationFailureProviderNotFound: ts: {} authentication: {} ex: {}",
			LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()),
			event.getAuthentication(),
			event.getException().getMessage()
		);
	}

	// =========== 鉴权类 ===========

	/**
	 * 鉴权成功 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthorizedEvent.class)
	@NewSpan
	public void authorizedEventListener(AuthorizedEvent event) {
		log.info("authorized: [authentication: {} attribute: {} ]",
			event.getAuthentication(),
			event.getConfigAttributes()
		);
	}

	/**
	 * 鉴权失败 事件监听
	 *
	 * @param event 事件
	 */
	@EventListener(AuthorizationFailureEvent.class)
	@NewSpan
	public void authorizationFailureEventListener(AuthorizationFailureEvent event) {
		log.info("AuthorizationFailureEvent: [ts: {}, authentication: {} attribute: {} ex: {}]",
			LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()),
			event.getAuthentication(),
			event.getConfigAttributes(),
			event.getAccessDeniedException().getMessage()
		);
	}

}
