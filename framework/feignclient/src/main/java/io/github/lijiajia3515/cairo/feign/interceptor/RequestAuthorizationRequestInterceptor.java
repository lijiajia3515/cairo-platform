package io.github.lijiajia3515.cairo.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求中获取凭证 拦截器
 */
@Slf4j
public class RequestAuthorizationRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		log.debug("template apply request authorization");
		final Collection<String> authorizations = template.headers().get(HttpHeaders.AUTHORIZATION);
		if (authorizations == null || authorizations.isEmpty()) {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
			Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).ifPresentOrElse(
				authorization -> {
					log.debug("request authorization: [{}]", authorization);
					template.header(HttpHeaders.AUTHORIZATION, authorization);
				},
				() -> log.debug("request authorization isEmpty")
			);
		}
	}

}
