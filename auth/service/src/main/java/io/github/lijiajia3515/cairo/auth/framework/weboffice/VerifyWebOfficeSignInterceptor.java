package io.github.lijiajia3515.cairo.auth.framework.weboffice;

import cn.hutool.crypto.SecureUtil;
import io.github.lijiajia3515.cairo.http.converter.AbstractHttpMessageHandler;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeError;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.WebofficeProperties;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model.WebofficeResult;
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
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class VerifyWebOfficeSignInterceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {
	/**
	 * 使用 RFC1123 时间格式的当前时间 请求头参数
	 */
	public static final String DATE_HEADER_NAME = "Date";

	/**
	 * 使用 RFC1123 时间格式的当前时间 请求头参数
	 */
	public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

	/**
	 * 使用 RFC1123 时间格式的当前时间 请求头参数
	 */
	public static final String CONTENT_MD5_HEADER_NAME = "Content-Md5";

	/**
	 * 使用 RFC1123 时间格式的当前时间 请求头参数
	 */
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";


	private final WebofficeProperties properties;

	public VerifyWebOfficeSignInterceptor(WebofficeProperties properties, List<HttpMessageConverter<?>> converters) {
		super(converters, null);
		this.properties = properties;
	}

	public VerifyWebOfficeSignInterceptor(WebofficeProperties properties, List<HttpMessageConverter<?>> messageConverters, ContentNegotiationManager contentNegotiationManager) {
		super(messageConverters, contentNegotiationManager);
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			//1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			VerifyWebOfficeSign classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), VerifyWebOfficeSign.class);
			//2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			VerifyWebOfficeSign methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), VerifyWebOfficeSign.class);
			VerifyWebOfficeSign token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}

			try {
				String date = request.getHeader(DATE_HEADER_NAME);
				String contentType = request.getContentType();
				String contentMd5 = request.getHeader(CONTENT_MD5_HEADER_NAME);
				String authorization = request.getHeader(AUTHORIZATION_HEADER_NAME);

				if (Stream.of(date, contentMd5, authorization).anyMatch(x -> x == null || x.isEmpty())) {
					log.debug("weboffice verify sign bad, reason: params is null");
					write(HttpStatus.BAD_REQUEST, WebOfficeError.SIGN_BAD, request, response);
					return false;
				}
				String rawStr = properties.getAppSecret() + contentMd5 + Optional.ofNullable(contentType).orElse("") + date;
				String encodeKey = SecureUtil.sha1(rawStr);
				String key = String.format("WPS-2:%s:%s", properties.getAppid(), encodeKey);
				if (key.equals(authorization)) {
					log.debug("weboffice verify sign");
					return true;
				}
			} catch (RuntimeException e) {
				log.debug("weboffice verify sign interceptor", e);
			}

			log.debug("weboffice verify sign bad");
			write(HttpStatus.BAD_REQUEST, WebOfficeError.SIGN_BAD, request, response);
			return false;
		}
		return true;
	}

	protected void write(HttpStatus httpStatus, WebOfficeError error, HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException, IOException {
		response.setStatus(httpStatus.value());

		WebofficeResult<Object> returnValue = WebofficeResult.builder()
			.code(error.getCode())
			.message(error.getMessage())
			.build();
		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

	}
}
