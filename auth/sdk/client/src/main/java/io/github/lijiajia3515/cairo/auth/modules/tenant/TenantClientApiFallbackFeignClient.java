package io.github.lijiajia3515.cairo.auth.modules.tenant;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-tenant fallback feignclient
 */
public class TenantClientApiFallbackFeignClient implements TenantClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<Tenant>>> getTenantList(String authorization, GetTenantArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Tenant>> getTenantInfo(String authorization, GetTenantInfoArgs args) {
		throw EX;
	}

}
