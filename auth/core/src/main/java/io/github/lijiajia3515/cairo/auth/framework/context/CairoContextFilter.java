package io.github.lijiajia3515.cairo.auth.framework.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.ENDPOINT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.ENDPOINT_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.APP_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.APP_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CAIRO_TAG;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CAIRO_TAG_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CLIENT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CLIENT_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_VERSION;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_VERSION_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.TENANT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.TENANT_ID_CONTEXT;

/**
 * cairo context filter
 */
public class CairoContextFilter extends GenericFilterBean {


	private final Map<String, CairoContextModel> CONTEXTS = new ConcurrentHashMap<>() {{
		put(CAIRO_TAG, CAIRO_TAG_CONTEXT);
		put(CLIENT_ID, CLIENT_ID_CONTEXT);
		put(ENDPOINT_ID, ENDPOINT_ID_CONTEXT);
		put(APP_ID, APP_ID_CONTEXT);
		put(TENANT_ID, TENANT_ID_CONTEXT);
		put(SUBAPP_ID, SUBAPP_ID_CONTEXT);
		put(SUBAPP_VERSION, SUBAPP_VERSION_CONTEXT);
	}};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		CONTEXTS.forEach((key, model) -> {
			String headerValue = httpRequest.getHeader(model.getHeaderName());
			String parameterValue = httpRequest.getParameter(model.getParameterName());
			if (StringUtils.hasText(headerValue)) {
				CairoContextHolder.setValue(key, headerValue);
			} else if (StringUtils.hasText(parameterValue)) {
				CairoContextHolder.setValue(key, parameterValue);
			} else {
				CairoContextHolder.setValue(key, model.getDefaultValue());
			}
		});
		chain.doFilter(request, response);
	}

	public void addContext(String key, CairoContextModel context) {
		CONTEXTS.put(key, context);
	}

	public void removeContext(String key) {
		CONTEXTS.remove(key);
	}

}
