package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.TenantSubapp;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-subapp fallback feignclient
 */
public class TenantSubappApiClientFallbackFeignClient implements TenantSubappApiClientFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");


	@Override
	public ResponseEntity<BusinessResult<List<TenantSubapp>>> getTenantSubappList(String authorization, GetTenantSubappArgs args) {
		throw EX;
	}
}
