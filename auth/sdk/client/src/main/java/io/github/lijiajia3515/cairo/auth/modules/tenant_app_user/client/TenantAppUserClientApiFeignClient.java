package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * [client/api] tenant app user feign client
 */
@FeignClient(
	contextId = "tenantAppUserClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_user",
	fallbackFactory = TenantAppUserClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface TenantAppUserClientApiFeignClient {

	/**
	 * 查询用户列表
	 * 需要权限： tenant_app_user:read｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户集合
	 */
	@PostMapping("/get_tenant_app_user_list")
	ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserArgs args);

	/**
	 * 查询用户分页
	 * 需要权限： tenant_app_user:read｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 分页对象
	 */
	@PostMapping("/get_tenant_app_user_page_list")
	ResponseEntity<BusinessResult<Page<TenantAppUser>>> getTenantAppUserPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserArgs args);

	/**
	 * 获取用户认证信息
	 * 需要权限： tenant_app_user:tenant_app_user_auth｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户信息
	 */
	@PostMapping("/get_tenant_app_user_auth")
	ResponseEntity<BusinessResult<TenantAppUserAuthModel>> getTenantAppUserAuth(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserAuthArgs args);

	@PostMapping("/get_tenant_app_user_sub_department_list")
	ResponseEntity<BusinessResult<List<TenantAppUser>>> getTenantAppUserSubDepartmentList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody(required = false) GetTenantAppUserArgs args);
}
