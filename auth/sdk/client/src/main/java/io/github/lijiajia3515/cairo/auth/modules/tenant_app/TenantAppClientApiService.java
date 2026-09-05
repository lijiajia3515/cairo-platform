package io.github.lijiajia3515.cairo.auth.modules.tenant_app;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface TenantAppClientApiService {


	/**
	 * 获取企业应用列表
	 *
	 * @param args 参数
	 * @return 企业 列表模式
	 */
	List<TenantApp> getTenantAppList(GetTenantAppArgs args);


	/**
	 * 获取企业应用分页列表
	 *
	 * @param args 参数
	 * @return 企业 分页模式
	 */
	Page<TenantApp> getTenantAppPageList(GetTenantAppArgs args);

}
