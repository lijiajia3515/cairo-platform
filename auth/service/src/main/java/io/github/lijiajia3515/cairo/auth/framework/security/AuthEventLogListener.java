//package io.github.lijiajia3515.cairo.auth.framework.security;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.security.access.event.AuthorizationFailureEvent;
//import org.springframework.security.access.event.AuthorizedEvent;
//import org.springframework.security.authentication.event.*;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//
//@Slf4j
//@Component
//public class AuthEventLogListener {
//	/**
//	 * 认证成功事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthorizedEvent.class)
//	public void authenticationSuccessEventListener(AuthenticationSuccessEvent event) {
//		log.debug("AuthenticationSuccessEvent: [ts: {} authentication: {} ]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication());
//	}
//
//	/**
//	 * 交互式方式认证成功 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(InteractiveAuthenticationSuccessEvent.class)
//	public void interactiveAuthenticationSuccessEventListener(InteractiveAuthenticationSuccessEvent event) {
//		log.debug("InteractiveAuthenticationSuccessEvent: [ts: {} authentication: {} class: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getGeneratedBy().getName());
//	}
//
//	/**
//	 * 登出成功 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(LogoutSuccessEvent.class)
//	public void logoutSuccessEventListener(LogoutSuccessEvent event) {
//		log.debug("LogoutSuccessEvent: [ts: {} authentication: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication());
//	}
//
//	/**
//	 * 认证凭证错误 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureBadCredentialsEvent.class)
//	public void authenticationFailureBadCredentialsEventListener(AuthenticationFailureBadCredentialsEvent event) {
//		log.info("AuthenticationFailureBadCredentials: [ts:{} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
//		log.debug("AuthenticationFailureBadCredentials", event.getException());
//	}
//
//	/**
//	 * 认证过期 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureExpiredEvent.class)
//	public void authenticationFailureExpiredEventListener(AuthenticationFailureExpiredEvent event) {
//		log.info("AuthenticationFailureExpiredEvent: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
//		log.debug("AuthenticationFailureExpiredEvent", event.getException());
//	}
//
//
//	/**
//	 * 用户被锁定 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureLockedEvent.class)
//	public void authenticationFailureLockedEventListener(AuthenticationFailureLockedEvent event) {
//		log.info("AuthenticationFailureLockedEvent: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
//		log.debug("AuthenticationFailureLockedEvent", event.getException());
//	}
//
//	/**
//	 * 用户被禁用 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureDisabledEvent.class)
//	public void authenticationFailureDisabledEventListener(AuthenticationFailureDisabledEvent event) {
//		log.info("AuthenticationFailureDisabledEvent: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
//		log.debug("AuthenticationFailureDisabledEvent", event.getException());
//	}
//
//	/**
//	 * 认证服务异常 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureServiceExceptionEvent.class)
//	public void authenticationFailureServiceExceptionEventListener(AuthenticationFailureServiceExceptionEvent event) {
//		log.debug("AuthenticationFailureServiceExceptionEvent: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException().getMessage());
//		log.info("AuthenticationFailureServiceExceptionEvent", event.getException());
//	}
//
//	/**
//	 * 认证提供方异常 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthenticationFailureProviderNotFoundEvent.class)
//	public void authenticationFailureProviderNotFoundEvent(AuthenticationFailureProviderNotFoundEvent event) {
//		log.debug("AuthenticationFailureProviderNotFound: [ts: {} authentication: {}, ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getException());
//		log.info("AuthenticationFailureProviderNotFound", event.getException());
//	}
//
//	// =========== 鉴权类 ===========
//
//	/**
//	 * 鉴权成功 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthorizedEvent.class)
//	public void authorizedEventListener(AuthorizedEvent event) {
//		log.info("authorized: [authentication: {} attribute: {} ]", event.getAuthentication(), event.getConfigAttributes());
//	}
//
//	/**
//	 * 鉴权失败 事件监听
//	 *
//	 * @param event 事件
//	 */
//	@EventListener(AuthorizationFailureEvent.class)
//	public void authorizationFailureEventListener(AuthorizationFailureEvent event) {
//		log.info("AuthorizationFailureEvent: [ts: {}, authentication: {} attribute: {} ex: {}]", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()), event.getAuthentication(), event.getConfigAttributes(), event.getAccessDeniedException().getMessage());
//		log.debug("AuthorizationFailureEvent", event.getAccessDeniedException());
//	}
//
//}
