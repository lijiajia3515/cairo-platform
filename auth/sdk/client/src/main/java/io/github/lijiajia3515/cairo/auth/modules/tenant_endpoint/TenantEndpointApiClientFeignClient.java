package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


/**
 * client-api-endpoint feignclient
 */
@FeignClient(
	contextId = "tenantEndpointClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_endpoint",
	fallbackFactory = TenantEndpointApiClientFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantEndpointApiClientFeignClient {


	/**
	 * 获取当前企业应用的终端列表
	 *
	 * @param args      参数
	 * @return 企业终端列表
	 */
	@PostMapping("/get_tenant_endpoint_list")
	ResponseEntity<BusinessResult<List<TenantEndpoint>>> getCurrentEndpointList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetCurrentEndpointArgs args);
}
