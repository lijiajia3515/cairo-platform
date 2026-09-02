package io.github.lijiajia3515.cairo.auth.framework.context;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class CairoContextRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		CairoContextHolder.getValue(CairoContextConstants.CAIRO_TAG).ifPresent(cairoTag -> template.header(CairoContextConstants.CAIRO_TAG_CONTEXT.getHeaderName(), cairoTag));
		CairoContextHolder.getValue(CairoContextConstants.CLIENT_ID).ifPresent(clientId -> template.header(CairoContextConstants.CLIENT_ID_CONTEXT.getHeaderName(), clientId));
		CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).ifPresent(endpointId -> template.header(CairoContextConstants.ENDPOINT_ID_CONTEXT.getHeaderName(), endpointId));
		CairoContextHolder.getValue(CairoContextConstants.APP_ID).ifPresent(appId -> template.header(CairoContextConstants.APP_ID_CONTEXT.getHeaderName(), appId));
		CairoContextHolder.getValue(CairoContextConstants.TENANT_ID).ifPresent(tenantId -> template.header(CairoContextConstants.TENANT_ID_CONTEXT.getHeaderName(), tenantId));
		CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).ifPresent(subappId -> template.header(CairoContextConstants.SUBAPP_ID_CONTEXT.getHeaderName(), subappId));
		CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).ifPresent(subappVersion -> template.header(CairoContextConstants.SUBAPP_VERSION_CONTEXT.getHeaderName(), subappVersion));
	}
}
