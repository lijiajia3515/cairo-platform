package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

import cn.hutool.crypto.SecureUtil;
import io.github.lijiajia3515.cairo.auth.framework.idempotent.IdempotentService;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
public class SignV1Interceptor extends AbstractHttpMessageHandler implements HandlerInterceptor {

	public Duration TIMEOUT_DURATION = Duration.ofMinutes(5);

	/**
	 * 幂等 请求头参数
	 */
	public static final String NONCE_HEADER_NAME = "Nonce";

	/**
	 * 幂等 路径参数
	 */
	public static final String NONCE_PARAMETER_NAME = "nonce";

	/**
	 * 签名 请求头参数
	 */
	public static final String TIMESTAMP_HEADER_NAME = "Timestamp";

	/**
	 * 时间戳 路径参数
	 */
	public static final String TIMESTAMP_PARAMETER_NAME = "timestamp";

	/**
	 *
	 */
	public static final String SIGN_HEADER_NAME = "Sign";

	/**
	 * 签名 路径参数
	 */
	public static final String SIGN_PARAMETER_NAME = "sign";

	private final IdempotentService idempotentService;

	private final SignProperties signProperties;

	public SignV1Interceptor(IdempotentService idempotentService, List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, SignProperties signProperties) {
		super(converters, manager);
		this.idempotentService = idempotentService;
		this.signProperties = signProperties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// debug 模式
		if (signProperties.isDebug()) {
			return true;
		}

		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			// 1.获取目标类上的目标注解（可判断目标类是否存在该注解）
			SignV1 classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), SignV1.class);
			// 2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
			SignV1 methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), SignV1.class);
			SignV1 token = methodToken != null ? methodToken : classToken;
			if (token == null) {
				return true;
			}
			String nonce = request.getHeader(NONCE_HEADER_NAME);
			if (nonce == null) nonce = request.getParameter(NONCE_PARAMETER_NAME);

			String timestamp = request.getHeader(TIMESTAMP_HEADER_NAME);
			if (timestamp == null) timestamp = request.getParameter(TIMESTAMP_PARAMETER_NAME);

			String sign = request.getHeader(SIGN_HEADER_NAME);
			if (sign == null) sign = request.getParameter(SIGN_PARAMETER_NAME);

			if (nonce == null || timestamp == null || sign == null) {
				// throw new SignException("时间错误", SignBusiness.BAD);
				write(HttpStatus.CONFLICT, SignBusiness.BAD, request, response);
				return false;
			}

			try {
				LocalDateTime requestDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestamp)), ZoneOffset.systemDefault());
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime max = now.plusSeconds(token.afterSeconds());
				LocalDateTime min = now.minusSeconds(token.beforeSeconds());
				if (requestDate.isBefore(min) || requestDate.isAfter(max)) {
					// throw new SignException("时间错误", SignBusiness.TIME_EXPIRED);
					write(HttpStatus.CONFLICT, SignBusiness.TIME_EXPIRED, request, response);
					return false;
				}
			} catch (RuntimeException e) {
				// throw new SignException("时间错误", SignBusiness.TIME_EXPIRED);
				write(HttpStatus.CONFLICT, SignBusiness.BAD, request, response);
				return false;
			}

			String encodeKey = "cairo:v1:" + SecureUtil.sha256(String.format("%s_%s", timestamp, nonce));
			if (!encodeKey.equals(sign)) {
				// throw new SignException("签名错误", SignBusiness.BAD);
				write(HttpStatus.CONFLICT, SignBusiness.BAD, request, response);
				return false;
			}

			// 幂等校验
			if (SignIdempotent.CONTINUE.equals(token.idempotent())) {
				// 继续执行
				return true;
			} else {
				boolean check = idempotentService.check(nonce, TIMEOUT_DURATION);

				// 强制校验
				if (SignIdempotent.REQUIRED.equals(token.idempotent()) && !check) {
					write(HttpStatus.CONFLICT, SignBusiness.REPEATED_REQUEST, request, response);
					return false;
				}

				// 失败跳过执行
				if (SignIdempotent.SKIP.equals(token.idempotent()) && !check) {
					write(HttpStatus.OK, DefaultBusiness.SUCCESS, request, response);
					return false;
				}
				// 正常执行
				return true;
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
