package io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc.GetOnlineTenantAppDocArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class TenantAppDocClientApiServiceImpl implements TenantAppDocClientApiService{

	private final TenantAppDocClientApiFeignClient tenantAppDocClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppDocClientApiServiceImpl(TenantAppDocClientApiFeignClient tenantAppDocClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppDocClientApiFeignClient = tenantAppDocClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public WebOfficeDocToken getPreviewTenantAppDocToken(GetOnlineTenantAppDocArgs args) {
		try {
			ResponseEntity<BusinessResult<WebOfficeDocToken>> previewAppDocToken = tenantAppDocClientApiFeignClient.getPreviewTenantAppDocToken(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(previewAppDocToken.getBody()).map(BusinessResult::getData).orElse(WebOfficeDocToken.builder().build());
		} catch (Exception e) {
			log.info("getPreviewAppDocToken：", e);
			throw new ConflictBusinessException("获取预览文档token失败");
		}
	}

	@Override
	public WebOfficeDocToken getEditTenantAppDocToken(GetOnlineTenantAppDocArgs args) {
		try {
			ResponseEntity<BusinessResult<WebOfficeDocToken>> previewAppDocToken = tenantAppDocClientApiFeignClient.getEditTenantAppDocToken(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(previewAppDocToken.getBody()).map(BusinessResult::getData).orElse(WebOfficeDocToken.builder().build());
		} catch (Exception e) {
			log.info("getEditAppDocToken：", e);
			throw new ConflictBusinessException("获取在线编辑文档url失败");
		}
	}
}
