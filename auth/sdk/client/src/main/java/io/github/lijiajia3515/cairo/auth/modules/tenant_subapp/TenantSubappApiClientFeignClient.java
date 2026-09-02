package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.TenantSubapp;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


/**
 * client-api-subapp feignclient
 */
@FeignClient(
	contextId = "tenantSubappClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_subapp",
	fallbackFactory = TenantSubappApiClientFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantSubappApiClientFeignClient {


	/**
	 * 获取当前企业应用的终端列表
	 *
	 * @param args      参数
	 * @return 企业终端列表
	 */
	@PostMapping("/get_tenant_subapp_list")
	ResponseEntity<BusinessResult<List<TenantSubapp>>> getTenantSubappList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantSubappArgs args);
}
