package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.AppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * [client/api] app user authorization feign client
 */
@FeignClient(
	contextId = "appUserAuthorizationClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/app_user_authorization",
	fallbackFactory = AppUserAuthorizationClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AppUserAuthorizationClientApiFeignClient {
	/**
	 * 获取应用总断认证
	 * 需要权限 app_user_authorization:get_app_user_authorization | app_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_app_user_authorization")
	ResponseEntity<BusinessResult<AppUserAuthorizationModel>> getAppUserAuthorization(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																									  @RequestBody GetAppUserAuthorizationArgs args);


}
