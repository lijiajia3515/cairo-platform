package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.GetTenantSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.TenantSubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * [client/api] tenant app subapp user authorization feignclient
 */
@FeignClient(
	contextId = "tenantSubappUserAuthorizationClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_subapp_user_authorization",
	fallbackFactory = TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantSubappUserAuthorizationClientApiFeignClient {
	/**
	 * 获取企业子应用用户认证
	 * 需要权限 tenant_subapp_user_authorization:get_tenant_subapp_user_authorization | tenant_subapp_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_tenant_subapp_user_authorization")
	ResponseEntity<BusinessResult<TenantSubappUserAuthorizationModel>> getTenantSubappUserAuthorization(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																												@RequestBody GetTenantSubappUserAuthorizationArgs args);


}
