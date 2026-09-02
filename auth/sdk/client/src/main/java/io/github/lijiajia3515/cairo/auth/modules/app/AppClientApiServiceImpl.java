package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class AppClientApiServiceImpl implements AppClientApiService {

	private final AppClientApiFeignClient appClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppClientApiServiceImpl(AppClientApiFeignClient appClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appClientApiFeignClient = appClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<App> getAppList(GetAppArgs args) {
		try {
			ResponseEntity<BusinessResult<List<App>>> appList = appClientApiFeignClient.getAppList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
				return Optional.ofNullable(appList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appList error", e);
			throw e;
		}
	}

	@Override
	public Page<App> getAppPageList(GetAppArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<App>>> appPageList = appClientApiFeignClient.getAppPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appPageList error", e);
			throw e;
		}
	}
}
