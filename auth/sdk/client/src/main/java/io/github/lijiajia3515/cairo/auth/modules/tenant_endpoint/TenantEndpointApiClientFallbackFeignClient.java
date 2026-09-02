package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-endpoint fallback feignclient
 */
public class TenantEndpointApiClientFallbackFeignClient implements TenantEndpointApiClientFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");


	@Override
	public ResponseEntity<BusinessResult<List<TenantEndpoint>>> getCurrentEndpointList(String authorization, GetCurrentEndpointArgs args) {
		throw EX;
	}
}
