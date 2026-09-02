package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class PermissionClientApiServiceImpl implements PermissionClientApiService {

	private final PermissionClientApiFeignClient permissionClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public PermissionClientApiServiceImpl(PermissionClientApiFeignClient permissionClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.permissionClientApiFeignClient = permissionClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<Permission> getPermissionList(GetPermissionListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Permission>>> permissionList = permissionClientApiFeignClient.getPermissionList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(permissionList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("permissionList error", e);
			throw e;
		}
	}

	@Override
	public List<Permission> getMyPermissionList(GetPermissionListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Permission>>> myPermissionList = permissionClientApiFeignClient.getMyPermissionList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(myPermissionList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("myPermissionList error", e);
			throw e;
		}
	}
}
