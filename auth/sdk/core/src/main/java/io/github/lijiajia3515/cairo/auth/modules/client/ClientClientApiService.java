package io.github.lijiajia3515.cairo.auth.modules.client;


import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;

import java.util.List;


public interface ClientClientApiService {
	/**
	 * 获取客户端基础信息列表
	 *
	 * @param args 参数
	 * @return 客户端基础信息
	 */
	List<BasicClient> getBasicClientList(GetClientArgs args);

	/**
	 * 获取客户端列表
	 *
	 * @param args 参数
	 * @return 客户端 列表模式
	 */
	List<Client> getClientList(GetClientArgs args);
}
