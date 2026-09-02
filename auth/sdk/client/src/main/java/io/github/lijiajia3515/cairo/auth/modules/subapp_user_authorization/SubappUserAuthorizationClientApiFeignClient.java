package io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * [client/api] app subapp user authorization feignclient
 */
@FeignClient(
	contextId = "subappUserAuthorizationClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/subapp_user_authorization",
	fallbackFactory = SubappUserAuthorizationClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface SubappUserAuthorizationClientApiFeignClient {
	/**
	 * 获取应用总断认证
	 * 需要权限 subapp_user_authorization:get_subapp_user_authorization | subapp_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_subapp_user_authorization")
	ResponseEntity<BusinessResult<SubappUserAuthorizationModel>> getSubappUserAuthorization(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																									@RequestBody GetSubappUserAuthorizationArgs args);


}
