package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
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
 * [client/api] tenant app role feign client
 */
@FeignClient(
	contextId = "tenantAppRoleClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_role",
	fallbackFactory = TenantAppRoleApiClientFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantAppRoleApiClientFeignClient {
	/**
	 * 获取角色列表
	 * 需要权限： tenant_app_role:read｜tenant_app_role:all
	 *
	 * @param args 参数
	 * @return 角色列表
	 */
	@PostMapping("/get_tenant_app_role_list")
	ResponseEntity<BusinessResult<List<MetadataTenantAppRole>>> getRoleList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppRoleArgs args);

	/**
	 * 获取角色分页列表
	 * 需要权限： tenant_app_role:read｜tenant_app_role:all
	 *
	 * @param args 参数
	 * @return 角色分页
	 */
	@PostMapping("/get_tenant_app_role_page_list")
	ResponseEntity<BusinessResult<Page<MetadataTenantAppRole>>> getRolePageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppRoleArgs args);

}
