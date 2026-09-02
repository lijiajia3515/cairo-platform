package io.github.lijiajia3515.cairo.auth.modules.tenant_app;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-tenant fallback feignclient
 */
public class TenantAppClientApiFallbackFeignClient implements TenantAppClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<TenantApp>>> getTenantAppList(String authorization, GetTenantAppArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<TenantApp>>> getTenantAppPageList(String authorization, GetTenantAppArgs args) {
		throw EX;
	}
}
