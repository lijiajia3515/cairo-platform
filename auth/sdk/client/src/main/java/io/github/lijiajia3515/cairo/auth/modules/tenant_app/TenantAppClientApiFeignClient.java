package io.github.lijiajia3515.cairo.auth.modules.tenant_app;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.core.page.Page;
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
	contextId = "tenantAppClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app",
	fallbackFactory = TenantAppClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantAppClientApiFeignClient {


	/**
	 * 获取租户应用列表
	 *
	 * @param args 参数
	 * @return 租户 列表模式
	 */
	@PostMapping("/get_tenant_app_list")
	ResponseEntity<BusinessResult<List<TenantApp>>> getTenantAppList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppArgs args);


	/**
	 * 获取租户应用分页列表
	 *
	 * @param args 参数
	 * @return 租户 分页模式
	 */
	@PostMapping("/get_tenant_app_page_list")
	ResponseEntity<BusinessResult<Page<TenantApp>>> getTenantAppPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetTenantAppArgs args);

}
