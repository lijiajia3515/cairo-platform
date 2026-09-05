package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

import cn.hutool.extra.servlet.JakartaServletUtil;
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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class CaptchaTokenInterceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {
	/**
	 * 请求头参数
	 */
	public static final String HEADER_NAME = "Captcha-Token";
	/**
	 * 路径参数
	 */
	public static final String PARAMETER_NAME = "captcha_token";
	private final CaptchaTokenService tokenStore;

	public CaptchaTokenInterceptor(CaptchaTokenService tokenStore, List<HttpMessageConverter<?>> converters) {
		super(converters, null);
		this.tokenStore = tokenStore;
	}

	public CaptchaTokenInterceptor(CaptchaTokenService tokenStore, List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager) {
		super(messageConverters, contentNegotiationManager);
		this.tokenStore = tokenStore;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			//1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			VerifyCaptchaToken classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), VerifyCaptchaToken.class);
			//2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			VerifyCaptchaToken methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), VerifyCaptchaToken.class);
			VerifyCaptchaToken token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}
			int maxFailCount = token.maxFailCount();
			String captchaToken = request.getHeader(HEADER_NAME);
			if (captchaToken == null) captchaToken = request.getParameter(PARAMETER_NAME);
			try {
				boolean result = tokenStore.verifyToken(VerifyCaptchaTokenArgs.builder()
					.token(captchaToken)
					.ip(JakartaServletUtil.getClientIP(request))
					.maxFailCount(maxFailCount)
					.build());

				if (result) {
					return true;
				}

			} catch (RuntimeException e) {
				log.info("captcha interceptor", e);
			}

			write(HttpStatus.CONFLICT, CaptchaTokenBusiness.BAD_TOKEN, request, response);

			return false;
		}
		return true;
	}

	protected void write(HttpStatus httpStatus, Business business, HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException, IOException {
		response.setStatus(httpStatus.value());

		// 验证码闸失败：显式标记可重试（409 默认不可重试，但可重新获取验证码后重试），
		// data 即该码族的结构化补充容器，retryHint 平铺其中，前端据此渲染重试入口
		BusinessResult<?> returnValue = BusinessResult.builder()
			.code(business.getCode())
			.message(business.getMessage())
			.retryable(true)
			.data(Map.of("retryHint", "验证码无效或已失效，请重新获取"))
			.build();
		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

	}
}
