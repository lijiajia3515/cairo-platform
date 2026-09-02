package io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc.GetOnlineTenantAppDocArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import org.springframework.http.ResponseEntity;

/**
 * tenant doc client api fallback feign client
 */
public class TenantAppDocClientApiFallbackFeignClient implements TenantAppDocClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-文档子应用故障");

	@Override
	public ResponseEntity<BusinessResult<WebOfficeDocToken>> getPreviewTenantAppDocToken(String authorization, GetOnlineTenantAppDocArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<WebOfficeDocToken>> getEditTenantAppDocToken(String authorization, GetOnlineTenantAppDocArgs args) {
		throw EX;
	}
}
