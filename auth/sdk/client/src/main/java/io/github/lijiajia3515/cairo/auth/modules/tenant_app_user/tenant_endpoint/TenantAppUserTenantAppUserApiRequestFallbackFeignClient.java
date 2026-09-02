package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * endpoint_user-api-user request fallback feignclient
 */
public class TenantAppUserTenantAppUserApiRequestFallbackFeignClient implements TenantAppUserTenantAppUserApiRequestFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserList(String authorization, GetTenantAppUserArgs param) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<TenantAppUser>>> getTenantAppUserPageList(String authorization, GetTenantAppUserArgs param) {
		throw EX;
	}
}
