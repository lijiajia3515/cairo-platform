package io.github.lijiajia3515.cairo.auth.modules.tenant;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 租户子应用-client模式feign客户端
 */
@FeignClient(
	contextId = "tenantClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant",
	fallbackFactory = TenantClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantClientApiFeignClient {

	/**
	 * 查询租户列表
	 * 需要权限: tenant:read | tenant:all
	 *
	 * @param args 参数
	 * @return 租户列表
	 */
	@PostMapping("/get_tenant_list")
	ResponseEntity<BusinessResult<List<Tenant>>> getTenantList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantArgs args);

	/**
	 * 获取单租户
	 * 需要权限: tenant:read | tenant:all
	 *
	 * @param args 参数
	 * @return 租户
	 */
	@PostMapping("/get_tenant_info")
	ResponseEntity<BusinessResult<Tenant>> getTenantInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetTenantInfoArgs args);

}
