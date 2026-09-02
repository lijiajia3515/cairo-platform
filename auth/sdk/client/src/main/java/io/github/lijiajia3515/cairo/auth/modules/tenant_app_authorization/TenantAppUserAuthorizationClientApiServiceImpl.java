package io.github.lijiajia3515.cairo.auth.modules.tenant_app_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class TenantAppUserAuthorizationClientApiServiceImpl implements TenantAppUserAuthorizationClientApiService {

	private final TenantAppUserAuthorizationClientApiFeignClient tenantAppUserAuthorizationClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppUserAuthorizationClientApiServiceImpl(TenantAppUserAuthorizationClientApiFeignClient tenantAppUserAuthorizationClientApiFeignClient,
														  CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppUserAuthorizationClientApiFeignClient = tenantAppUserAuthorizationClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public TenantAppUserAuthorizationModel getTenantAppUserAuthorization(GetTenantAppUserAuthorizationArgs args) {
		try {
			ResponseEntity<BusinessResult<TenantAppUserAuthorizationModel>> authorization = tenantAppUserAuthorizationClientApiFeignClient.getTenantAppUserAuthorization(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(authorization.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getTenantAppUserAuthorization error", e);
			throw e;
		}
	}
}
