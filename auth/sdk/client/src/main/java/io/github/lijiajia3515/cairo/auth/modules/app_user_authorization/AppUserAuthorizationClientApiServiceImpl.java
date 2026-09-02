package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.AppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class AppUserAuthorizationClientApiServiceImpl implements AppUserAuthorizationClientApiService {

	private final AppUserAuthorizationClientApiFeignClient appUserAuthorizationClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppUserAuthorizationClientApiServiceImpl(AppUserAuthorizationClientApiFeignClient appUserAuthorizationClientApiFeignClient,
															CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appUserAuthorizationClientApiFeignClient = appUserAuthorizationClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public AppUserAuthorizationModel getAppUserAuthorization(GetAppUserAuthorizationArgs args) {
		try {
			ResponseEntity<BusinessResult<AppUserAuthorizationModel>> authorization = appUserAuthorizationClientApiFeignClient.getAppUserAuthorization(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(authorization.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAppUserAuthorization error", e);
			throw e;
		}
	}
}
