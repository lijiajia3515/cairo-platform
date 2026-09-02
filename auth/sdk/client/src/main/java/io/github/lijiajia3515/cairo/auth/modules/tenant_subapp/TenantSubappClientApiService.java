package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.TenantSubapp;

import java.util.List;

public interface TenantSubappClientApiService {

	/**
	 * 获取当前企业应用的应用子应用列表
	 *
	 * @param args      参数
	 * @return 企业子应用列表
	 */
	List<TenantSubapp> getTenantSubappList(GetTenantSubappArgs args);
}
