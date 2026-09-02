package io.github.lijiajia3515.cairo.web.filter;

import io.github.lijiajia3515.cairo.core.cairotag.CairoTagContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 灰度标签过滤器
 */
public class CairoTagContextFilter extends GenericFilterBean {
	public static final String DEFAULT_HEADER_NAME = "x-cairo-tag";
	public static final String DEFAULT_QUERY_PARAMETER_NAME = "x_cairo_tag";
	private String headerName = DEFAULT_HEADER_NAME;
	private String queryParameterName = DEFAULT_QUERY_PARAMETER_NAME;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String cairoTagHeaderValue = httpRequest.getHeader(headerName);
		String cairoTagQueryParameterValue = httpRequest.getParameter(queryParameterName);
		if (StringUtils.hasText(cairoTagHeaderValue)) {
			CairoTagContextHolder.setCairoTag(cairoTagHeaderValue);
		} else if (StringUtils.hasText(cairoTagQueryParameterValue)) {
			CairoTagContextHolder.setCairoTag(cairoTagQueryParameterValue);
		} else {
			CairoTagContextHolder.setCairoTag(CairoTagContextHolder.DEFAULT_TAG);
		}
		chain.doFilter(request, response);
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getQueryParameterName() {
		return queryParameterName;
	}

	public void setQueryParameterName(String queryParameterName) {
		this.queryParameterName = queryParameterName;
	}
}
