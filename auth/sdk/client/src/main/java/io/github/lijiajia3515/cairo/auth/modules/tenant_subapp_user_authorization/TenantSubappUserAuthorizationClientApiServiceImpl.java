package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.GetTenantSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.TenantSubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class TenantSubappUserAuthorizationClientApiServiceImpl implements TenantSubappUserAuthorizationClientApiService {

	private final TenantSubappUserAuthorizationClientApiFeignClient tenantSubappUserAuthorizationClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantSubappUserAuthorizationClientApiServiceImpl(TenantSubappUserAuthorizationClientApiFeignClient tenantSubappUserAuthorizationClientApiFeignClient,
																 CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantSubappUserAuthorizationClientApiFeignClient = tenantSubappUserAuthorizationClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public TenantSubappUserAuthorizationModel getTenantSubappUserAuthorization(GetTenantSubappUserAuthorizationArgs args) {
		try {
			ResponseEntity<BusinessResult<TenantSubappUserAuthorizationModel>> authorization = tenantSubappUserAuthorizationClientApiFeignClient.getTenantSubappUserAuthorization(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(authorization.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getTenantAppUserAuthorization error", e);
			throw e;
		}
	}
}
