package io.github.lijiajia3515.cairo.auth.modules.tenant_app_authorization;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * [client/api] tenant app user authorization feignclient
 */
@FeignClient(
	contextId = "tenantAppUserAuthorizationClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_user_authorization",
	fallbackFactory = TenantAppUserAuthorizationClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantAppUserAuthorizationClientApiFeignClient {
	/**
	 * 获取企业子应用级用户认证
	 * 需要权限 tenant_app_user_authorization:get_tenant_app_user_authorization | tenant_app_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_tenant_app_user_authorization")
	ResponseEntity<BusinessResult<TenantAppUserAuthorizationModel>> getTenantAppUserAuthorization(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																								  @RequestBody GetTenantAppUserAuthorizationArgs args);


}
