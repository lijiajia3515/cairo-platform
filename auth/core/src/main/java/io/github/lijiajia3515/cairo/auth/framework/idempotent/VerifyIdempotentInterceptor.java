package io.github.lijiajia3515.cairo.auth.framework.idempotent;

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
import java.time.Duration;
import java.util.List;

@Slf4j
public class VerifyIdempotentInterceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {

	/**
	 * 请求头参数
	 */
	public static final String HEADER_NAME = "Idempotent-Token";
	/**
	 * 路径参数
	 */
	public static final String PARAMETER_NAME = "idempotent_token";

	private final IdempotentService idempotentService;

	public VerifyIdempotentInterceptor(IdempotentService idempotentService, List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager) {
		super(converters, manager);
		this.idempotentService = idempotentService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			//1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			VerifyIdempotent classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), VerifyIdempotent.class);
			//2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			VerifyIdempotent methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), VerifyIdempotent.class);
			VerifyIdempotent token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}
			String idempotentToken = request.getHeader(HEADER_NAME);
			if (idempotentToken == null) idempotentToken = request.getParameter(PARAMETER_NAME);
			boolean check = idempotentService.check(idempotentToken, Duration.ofHours(1));

			if (!check) {
				write(HttpStatus.CONFLICT, IdempotentBusiness.REPEATED_REQUEST, request, response);
				return false;
			}
			return true;
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
