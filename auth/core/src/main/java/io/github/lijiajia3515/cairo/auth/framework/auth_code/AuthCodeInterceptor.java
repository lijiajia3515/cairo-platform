package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.ServiceBusiness;
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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AuthCodeInterceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {
	/**
	 * 请求头参数
	 */
	public static final String HEADER_NAME = "Auth-Code";
	/**
	 * 路径参数
	 */
	public static final String PARAMETER_NAME = "auth_code";
	private final AuthCodeVerifyService authCodeVerifyService;

	public AuthCodeInterceptor(AuthCodeVerifyService authCodeVerifyService, List<HttpMessageConverter<?>> converters) {
		super(converters, null);
		this.authCodeVerifyService = authCodeVerifyService;
	}

	public AuthCodeInterceptor(AuthCodeVerifyService authCodeVerifyService, List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager) {
		super(messageConverters, contentNegotiationManager);
		this.authCodeVerifyService = authCodeVerifyService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			//1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			AuthCode classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), AuthCode.class);
			//2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			AuthCode methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), AuthCode.class);
			AuthCode token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}

			String accountId = CairoSecurityContextHolder.getAccountId();
			String authCodeToken = request.getHeader(HEADER_NAME);
			if (authCodeToken == null) authCodeToken = request.getParameter(PARAMETER_NAME);

			if (accountId == null || authCodeToken == null) {
				write(HttpStatus.CONFLICT, AuthCodeBusiness.PARAMS_ERROR, request, response);
				return false;
			}

			try {
				AuthCodeVerifyStat stat = authCodeVerifyService.verify(VerifyAuthCodeArgs.builder()
					.accountId(accountId)
					.authCode(authCodeToken)
					.build());
				if (AuthCodeVerifyStat.FAILED.equals(stat)) {
					write(HttpStatus.CONFLICT, AuthCodeBusiness.BAD, request, response);
					return false;
				}
				if (AuthCodeVerifyStat.EXPIRED.equals(stat)) {
					write(HttpStatus.CONFLICT, AuthCodeBusiness.EXPIRED, request, response);
					return false;
				}
				return true;
			} catch (RuntimeException e) {
				log.info("auth_code verify interceptor", e);
				write(HttpStatus.INTERNAL_SERVER_ERROR, ServiceBusiness.ERROR, request, response);
				return false;
			}
		}
		return true;
	}

	protected void write(HttpStatus httpStatus, Business business, HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException, IOException {
		response.setStatus(httpStatus.value());

		BusinessResult<?> returnValue = BusinessResult.builder()
			.business(business)
			.build();
		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

	}
}
