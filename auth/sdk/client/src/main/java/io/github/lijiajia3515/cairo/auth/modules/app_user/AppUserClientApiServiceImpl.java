package io.github.lijiajia3515.cairo.auth.modules.app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AppUserClientApiServiceImpl implements AppUserClientApiService {

	private final AppUserClientApiFeignClient appUserClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppUserClientApiServiceImpl(AppUserClientApiFeignClient appUserClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appUserClientApiFeignClient = appUserClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<AppUser> getAppUserList(GetAppUserClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<AppUser>>> appUserList = appUserClientApiFeignClient.getAppUserList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appUserList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("appUserList error", e);
			throw e;
		}
	}

	@Override
	public Page<AppUser> getAppUserPageList(GetAppUserClientArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<AppUser>>> appUserPageList = appUserClientApiFeignClient.getAppUserPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appUserPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appUserPageList error", e);
			throw e;
		}
	}

	@Override
	public AppUserAuthModel getAppUserAuth(GetAppUserAuthArgs args) {
		try {
			ResponseEntity<BusinessResult<AppUserAuthModel>> appUserAuth = appUserClientApiFeignClient.getAppUserAuth(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appUserAuth.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appUserAuth error", e);
			throw e;
		}
	}
}
