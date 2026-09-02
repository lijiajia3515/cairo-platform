package io.github.lijiajia3515.cairo.auth.framework.security.cairo_security;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.http.converter.AbstractHttpMessageHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * 用户类型拦截器验证
 */
@Slf4j
public class CairoSecurityInterceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {
	private final CairoSecurityProperties properties;

	public CairoSecurityInterceptor(CairoSecurityProperties properties, List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager) {
		super(converters, manager);
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {

			HandlerMethod handlerMethod = (HandlerMethod) handler;
			//1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			CairoSecurity classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), CairoSecurity.class);
			//2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			CairoSecurity methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), CairoSecurity.class);
			CairoSecurity token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}
			CairoSecurityType type = token.type();
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
				// 未登录
				write(HttpStatus.UNAUTHORIZED, CairoAuthBusiness.UNAUTHORIZED, request, response);
				return false;
			}
			if (CairoSecurityType.CLIENT.equals(type) && authentication instanceof CairoOAuthClientAuthenticationToken) {
				return true;
			}

			if (CairoSecurityType.ACCOUNT.equals(type) && authentication instanceof CairoOAuthAccountAuthenticationToken) {
				return true;
			}


			if (CairoSecurityType.APP_USER.equals(type) && authentication instanceof CairoOAuthAppUserAuthenticationToken) {
				return true;
			}

			if (CairoSecurityType.SUBAPP_USER.equals(type) && authentication instanceof CairoOAuthSubappUserAuthenticationToken) {
				return true;
			}

			if (CairoSecurityType.TENANT_APP_USER.equals(type) && authentication instanceof CairoOAuthTenantAppUserAuthenticationToken) {
				return true;
			}

			if (CairoSecurityType.TENANT_SUBAPP_USER.equals(type) && authentication instanceof CairoOAuthTenantSubappUserAuthenticationToken) {
				return true;
			}

			if (CairoSecurityType.CAIRO_CLIENT.equals(type) && authentication instanceof CairoOAuthClientAuthenticationToken) {
				String appId = CairoSecurityContextHolder.getClient().map(CairoOAuthClientPrincipal::getAppId).orElse(null);
				if (properties.getCairoAppId().equals(appId)) {
					return true;
				}
			}

			if (CairoSecurityType.CAIRO_APP_USER.equals(type) && authentication instanceof CairoOAuthAppUserAuthenticationToken) {
				CairoOAuthAppUserPrincipal principal = CairoSecurityContextHolder.getAppUser().orElse(null);
				if (principal != null) {
					if (principal.getAppId().equals(properties.getCairoAppId())) {
						return true;
					}
				}
			}

			if (CairoSecurityType.CAIRO_WEB_MANAGE_USER.equals(type) && authentication instanceof CairoOAuthSubappUserAuthenticationToken) {
				CairoOAuthSubappUserPrincipal principal = CairoSecurityContextHolder.getSubappUser().orElse(null);
				if (principal != null) {
					if (principal.getAppId().equals(properties.getCairoAppId()) && principal.getSubappId().equals(properties.getManageSubappId())) {
						return true;
					}
				}
			}

			// 错误Token
			write(HttpStatus.UNAUTHORIZED, CairoAuthBusiness.NOT_SUPPORTED, request, response);
			return false;
		}
		return true;
	}


	protected void write(HttpStatus httpStatus, Business business, HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException, IOException {
		response.setStatus(httpStatus.value());

		BusinessResult<Object> returnValue = BusinessResult.builder()
			.business(business)
			.build();
		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

	}
}
