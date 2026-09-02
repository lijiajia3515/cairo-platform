package io.github.lijiajia3515.cairo.auth.modules.account_authorization.account;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * client-api account authorization feignclient
 */
@FeignClient(
	contextId = "accountAuthorizationClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/account_authorization",
	fallbackFactory = AccountAuthorizationClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AccountAuthorizationClientApiFeignClient {
	/**
	 * 获取账号认证
	 * 需要权限 account_authorization:get_account_authorization | account:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_account_authorization")
	ResponseEntity<BusinessResult<AccountAuthorizationModel>> getAccountAuthorization(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																					  @RequestBody GetAccountAuthorizationArgs args);


}
