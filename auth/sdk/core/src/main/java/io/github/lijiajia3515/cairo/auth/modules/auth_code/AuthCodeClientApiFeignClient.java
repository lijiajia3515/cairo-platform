package io.github.lijiajia3515.cairo.auth.modules.auth_code;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * client-api-auth_code feignclient
 */
@FeignClient(
	contextId = "authCodeClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/auth_code",
	fallbackFactory = AuthCodeClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface AuthCodeClientApiFeignClient {

	/**
	 * authCode token 校验
	 * 需要权限 auth_code:verify_token
	 *
	 * @param args args
	 * @return 验证是否通过
	 */
	@PostMapping("/verify_auth_code")
	ResponseEntity<BusinessResult<AuthCodeVerifyStat>> verifyAuthCode(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody VerifyAuthCodeArgs args);

}
