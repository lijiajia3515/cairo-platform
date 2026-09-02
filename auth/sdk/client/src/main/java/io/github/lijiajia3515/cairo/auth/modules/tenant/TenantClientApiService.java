package io.github.lijiajia3515.cairo.auth.modules.tenant;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;

import java.util.List;


public interface TenantClientApiService {

	/**
	 * 查询租户列表
	 * 需要权限: tenant:read | tenant:all
	 *
	 * @param args 参数
	 * @return 租户列表
	 */
	List<Tenant> getTenantList(GetTenantArgs args);

	/**
	 * 获取单租户
	 * 需要权限: tenant:read | tenant:all
	 *
	 * @param args 参数
	 * @return 租户
	 */
	Tenant getTenantInfo(GetTenantInfoArgs args);

}
