package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code.SendAccountPhoneNumberVerifyCodeArgs;
import io.github.lijiajia3515.cairo.auth.modules.account_sns.AccountSnsClientApiFeignClientFallbackFactory;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;

/**
 * client api verify_code connect feignclient
 */
@FeignClient(contextId = "verifyCodeClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/verify_code",
	fallbackFactory = AccountSnsClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class)
public interface VerifyCodeClientApiFeignClient {

	/**
	 * 发送账号手机号验证码
	 * 需要权限： verify_code:send_account_phone_number_verify_code ｜verify_code:all
	 *
	 * @return 空
	 */
	@PostMapping("/send_account_phone_number_verify_code")
	ResponseEntity<BusinessResult< Optional<String>>> sendAccountPhoneNumberVerifyCode(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody SendAccountPhoneNumberVerifyCodeArgs args);
}
