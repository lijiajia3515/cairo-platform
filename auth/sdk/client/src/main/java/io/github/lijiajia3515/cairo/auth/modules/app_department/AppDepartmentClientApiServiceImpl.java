package io.github.lijiajia3515.cairo.auth.modules.app_department;


import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AppDepartmentClientApiServiceImpl implements AppDepartmentClientApiService {

	private final AppDepartmentClientApiFeignClient appDepartmentClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppDepartmentClientApiServiceImpl(AppDepartmentClientApiFeignClient appDepartmentClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appDepartmentClientApiFeignClient = appDepartmentClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<AppDepartment> getAppDepartmentList(GetAppDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<List<AppDepartment>>> appDepartmentList = appDepartmentClientApiFeignClient.getAppDepartmentList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appDepartmentList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("appDepartmentList error", e);
			throw e;
		}
	}

	@Override
	public Page<AppDepartment> getAppDepartmentPageList(GetAppDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<AppDepartment>>> appDepartmentPageList = appDepartmentClientApiFeignClient.getAppDepartmentPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(appDepartmentPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appDepartmentPageList error", e);
			throw e;
		}
	}
}
