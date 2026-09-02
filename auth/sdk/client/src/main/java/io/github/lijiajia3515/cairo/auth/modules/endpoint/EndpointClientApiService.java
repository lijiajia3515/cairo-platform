package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface EndpointClientApiService {

	/**
	 * 获取app终端列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	List<Endpoint> getEndpointList(GetEndpointClientArgs args);

	/**
	 * 获取app终端分页列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	Page<Endpoint> getEndpointPageList(GetEndpointClientArgs args);

	/**
	 * 获取app终端列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	List<Endpoint> getEndpointByAppList(GetEndpointByAppClientArgs args);
}
