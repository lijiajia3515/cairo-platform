package io.github.lijiajia3515.cairo.auth.modules.app_doc.client;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_doc.GetPreviewAppDocTokenArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import org.springframework.http.ResponseEntity;

/**
 * tenant doc client api fallback feign client
 */
public class AppDocClientApiFallbackFeignClient implements AppDocClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-文档子应用故障");


	@Override
	public ResponseEntity<BusinessResult<WebOfficeDocToken>> getPreviewAppDocToken(String authorization, GetPreviewAppDocTokenArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<WebOfficeDocToken>> getEditAppDocToken(String authorization, GetPreviewAppDocTokenArgs args) {
		throw EX;
	}
}
