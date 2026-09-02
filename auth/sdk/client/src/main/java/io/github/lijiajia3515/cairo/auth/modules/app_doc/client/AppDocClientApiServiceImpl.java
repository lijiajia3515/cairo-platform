package io.github.lijiajia3515.cairo.auth.modules.app_doc.client;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_doc.GetPreviewAppDocTokenArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;;

@Slf4j
public class AppDocClientApiServiceImpl implements AppDocClientApiService {

	private final AppDocClientApiFeignClient appDocClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AppDocClientApiServiceImpl(AppDocClientApiFeignClient appDocClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.appDocClientApiFeignClient = appDocClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public WebOfficeDocToken getPreviewAppDocToken(GetPreviewAppDocTokenArgs args) {
		try {
			ResponseEntity<BusinessResult<WebOfficeDocToken>> previewAppDocToken = appDocClientApiFeignClient.getPreviewAppDocToken(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(previewAppDocToken.getBody()).map(BusinessResult::getData).orElse(WebOfficeDocToken.builder().build());
		} catch (Exception e) {
			log.info("getPreviewAppDocToken：", e);
			throw new ConflictBusinessException("获取预览文档token失败");
		}
	}

	@Override
	public WebOfficeDocToken getEditAppDocToken(GetPreviewAppDocTokenArgs args) {
		try {
			ResponseEntity<BusinessResult<WebOfficeDocToken>> previewAppDocToken = appDocClientApiFeignClient.getEditAppDocToken(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(previewAppDocToken.getBody()).map(BusinessResult::getData).orElse(WebOfficeDocToken.builder().build());
		} catch (Exception e) {
			log.info("getEditAppDocToken：", e);
			throw new ConflictBusinessException("获取在线编辑文档url失败");
		}
	}
}
