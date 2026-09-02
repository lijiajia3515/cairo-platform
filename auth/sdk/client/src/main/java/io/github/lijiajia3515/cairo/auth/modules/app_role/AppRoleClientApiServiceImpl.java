package io.github.lijiajia3515.cairo.auth.modules.app_role;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AppRoleClientApiServiceImpl implements AppRoleClientApiService {

	private final AppRoleClientApiFeignClient appRoleClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppRoleClientApiServiceImpl(AppRoleClientApiFeignClient appRoleClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appRoleClientApiFeignClient = appRoleClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<MetadataAppRole> getAppRoleList(GetAppRoleArgs args) {
		try {
			ResponseEntity<BusinessResult<List<MetadataAppRole>>> appRoleList = appRoleClientApiFeignClient.getAppRoleList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appRoleList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("appRoleList error", e);
			throw e;
		}
	}

	@Override
	public Page<MetadataAppRole> getAppRolePageList(GetAppRoleArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<MetadataAppRole>>> appRolePageList = appRoleClientApiFeignClient.getAppRolePageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appRolePageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appRolePageList error", e);
			throw e;
		}
	}
}
