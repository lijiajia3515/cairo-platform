package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;

import java.util.List;

public interface TenantEndpointClientApiService {

	/**
	 * 获取当前企业应用的终端列表
	 *
	 * @param args      参数
	 * @return 企业终端列表
	 */
	List<TenantEndpoint> getCurrentEndpointList(GetCurrentEndpointArgs args);
}
