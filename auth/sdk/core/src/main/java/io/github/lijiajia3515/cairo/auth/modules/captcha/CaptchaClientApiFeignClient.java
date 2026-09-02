package io.github.lijiajia3515.cairo.auth.modules.captcha;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * client-api-captcha feignclient
 */
@FeignClient(
	contextId = "captchaClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/captcha",
	fallbackFactory = CaptchaClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface CaptchaClientApiFeignClient {

	/**
	 * 验证行为验证码token
	 * 需要权限 captcha:verify_token | captcha:all
	 *
	 * @param args args
	 * @return 验证是否通过
	 */
	@PostMapping("/verify_captcha_token")
	ResponseEntity<BusinessResult<Boolean>> verifyCaptchaToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody VerifyCaptchaTokenArgs args);

}
