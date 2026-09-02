package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetCustomTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * [client/api] tenant app endpoint user authorization fallback feignclient
 */
public class TenantAppUserAuthorizationClientApiFallbackFeignClient implements TenantAppUserAuthorizationClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-企业终端用户授权子应用故障");


	@Override
	public ResponseEntity<BusinessResult<TenantAppUserAuthorizationModel>> getTenantAppUserAuthorization(String authorization, GetTenantAppUserAuthorizationArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<TenantAppUserAuthorizationModel>> getCustomTenantAppUserAuthorization(String authorization, GetCustomTenantAppUserAuthorizationArgs args) {
		throw EX;
	}
}
