package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.GetTenantSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.TenantSubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * [client/api] tenant app subapp user authorization fallback feignclient
 */
public class TenantSubappUserAuthorizationClientApiFallbackFeignClient implements TenantSubappUserAuthorizationClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-企业子应用用户授权子应用故障");


	@Override
	public ResponseEntity<BusinessResult<TenantSubappUserAuthorizationModel>> getTenantSubappUserAuthorization(String authorization, GetTenantSubappUserAuthorizationArgs args) {
		throw EX;
	}
}
