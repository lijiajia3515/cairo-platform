package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * [client/api] tenant app user fallback feignclient
 */
public class TenantAppUserClientApiFallbackFeignClient implements TenantAppUserClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-企业应用级用户-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserList(String authorization, GetTenantAppUserArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<TenantAppUser>>> getTenantAppUserPageList(String authorization, GetTenantAppUserArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<TenantAppUserAuthModel>> getTenantAppUserAuth(String authorization, GetTenantAppUserAuthArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserSubDepartmentList(String authorization, GetTenantAppUserArgs args) {
		throw EX;
	}
}
