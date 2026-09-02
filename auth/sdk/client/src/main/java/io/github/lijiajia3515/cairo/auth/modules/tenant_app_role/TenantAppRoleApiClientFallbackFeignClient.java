package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 容错实现 的 role client
 */
public class TenantAppRoleApiClientFallbackFeignClient implements TenantAppRoleApiClientFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<MetadataTenantAppRole>>> getRoleList(String authorization, GetTenantAppRoleArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<MetadataTenantAppRole>>> getRolePageList(String authorization, GetTenantAppRoleArgs args) {
		throw EX;
	}
}
