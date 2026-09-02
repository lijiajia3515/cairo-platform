package io.github.lijiajia3515.cairo.autoconfigure.rabbitmq;//package com.yr.cairo.auth.framework.rabbitmq;
//
//import com.yr.cairo.auth.famework.security.CairoAuthenticationToken;
//import com.yr.cairo.auth.famework.security.authentication.CairoUserAuthenticationToken;
//import com.yr.cairo.auth.modules.CairoAuthBasicFeignClient;
//import com.yr.cairo.auth.modules.auth.CairoAuthenticationModel;
//import lombok.extern.slf4j.Slf4j;
//import org.aopalliance.intercept.MethodInterceptor;
//import org.aopalliance.intercept.MethodInvocation;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.springframework.amqp.core.Message;
//import org.springframework.http.HttpHeaders;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class RabbitListenerAdvice implements MethodInterceptor {
//
//	private final CairoAuthBasicFeignClient cairoAuthBasicFeignClient;
//
//	public RabbitListenerAdvice(CairoAuthBasicFeignClient cairoAuthBasicFeignClient) {
//		this.cairoAuthBasicFeignClient = cairoAuthBasicFeignClient;
//	}
//
//	@Nullable
//	@Override
//	public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
//
//		Object[] args = invocation.getArguments();
//		for (Object obj : args) {
//			if (obj instanceof Message) {
//
//				try {
//					Map<String, Object> headers = ((Message) obj).getMessageProperties().getHeaders();
//					String tenantId = (String) headers.get("Tenant-Id");
//					String authorization = (String) headers.get(HttpHeaders.AUTHORIZATION);
//					if (tenantId != null && authorization != null) {
//						CairoAuthenticationModel cairoAuthenticationModel = cairoAuthBasicFeignClient.authGetAuth(authorization, tenantId);
//						CairoUserAuthenticationToken token = new CairoUserAuthenticationToken(cairoAuthenticationModel.getPrincipal(), cairoAuthenticationModel.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet()));
//						SecurityContextHolder.getContext().setAuthentication(token);
//
//					}
//				} catch (Exception e) {
//					log.error("", e);
//				}
//			}
//		}
//
//
//		return invocation.proceed();
//	}
//
//}
