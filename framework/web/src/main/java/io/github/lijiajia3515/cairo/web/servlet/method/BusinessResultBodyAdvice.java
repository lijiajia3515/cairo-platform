package io.github.lijiajia3515.cairo.web.servlet.method;

import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;
import java.util.Optional;

@RestControllerAdvice
public class BusinessResultBodyAdvice implements ResponseBodyAdvice<Object> {
	public static final String HEADER_NAME = "Cairo-Business";
	public static final String PARAM_NAME = "cairo_business";

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		if (annotation(returnType) || request(request)) {
			return BusinessResult.builder().business(DefaultBusiness.SUCCESS).data(body).build();
		}
		return body;
	}

	boolean annotation(MethodParameter returnType) {
		return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), BusinessResultBody.class) ||
			returnType.hasMethodAnnotation(BusinessResultBody.class));
	}

	boolean request(ServerHttpRequest request) {
		return Optional.ofNullable(request.getHeaders().get(HEADER_NAME))
			.or(() -> Optional.ofNullable(((ServletServerHttpRequest) request).getServletRequest().getParameterValues(PARAM_NAME)).map(Arrays::asList))
			.filter(x -> !x.isEmpty()).isPresent();
	}
}
