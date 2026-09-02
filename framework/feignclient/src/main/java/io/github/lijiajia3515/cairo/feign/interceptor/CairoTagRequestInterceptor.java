package io.github.lijiajia3515.cairo.feign.interceptor;

import io.github.lijiajia3515.cairo.core.cairotag.CairoTagContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.Getter;
import lombok.Setter;

/**
 * 灰度标签过滤器
 */
public class CairoTagRequestInterceptor implements RequestInterceptor {
	public static final String DEFAULT_HEADER_NAME = "x-cairo-tag";
	public static final String DEFAULT_QUERY_PARAMETER_NAME = "x_cairo_tag";
	@Getter
	@Setter
	private String headerName = DEFAULT_HEADER_NAME;

	@Getter
	@Setter
	private String queryParameterName = DEFAULT_QUERY_PARAMETER_NAME;

	@Override
	public void apply(RequestTemplate template) {
		String tenantId = CairoTagContextHolder.getCairoTag();
		template.header(headerName, tenantId);
	}
}
