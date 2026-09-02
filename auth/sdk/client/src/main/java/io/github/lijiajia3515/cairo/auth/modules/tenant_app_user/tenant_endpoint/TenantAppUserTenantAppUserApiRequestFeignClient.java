package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.framework.feign.CairoRequestFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
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
 * [tenant_app_user] tenant app user feign client
 */
@FeignClient(
	contextId = "tenantAppUserEndpointUserApiRequestFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/tenant_app_user_api/tenant_app_user",
	fallbackFactory = TenantAppUserTenantAppUserApiRequestFeignClientFallbackFactory.class,
	configuration = CairoRequestFeignClientConfiguration.class
)
public interface TenantAppUserTenantAppUserApiRequestFeignClient {

	/**
	 * 获取企业用户列表
	 * 需要权限 tenant_app_user:read ｜ tenant_app_user:all
	 *
	 * @param param args
	 * @return 用户集合
	 */
	@PostMapping("/get_tenant_app_user_list")
	ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserArgs param);

	/**
	 * 获取企业用户分页列表
	 * 需要权限 tenant_app_user:read ｜ tenant_app_user:all
	 *
	 * @param param args
	 * @return 用户分页集合
	 */
	@PostMapping("/get_tenant_app_user_page_list")
	ResponseEntity<BusinessResult<Page<TenantAppUser>>> getTenantAppUserPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserArgs param);


}
