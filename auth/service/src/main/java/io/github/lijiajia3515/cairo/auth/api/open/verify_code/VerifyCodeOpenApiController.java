package io.github.lijiajia3515.cairo.auth.api.open.verify_code;

import io.github.lijiajia3515.cairo.auth.modules.captcha.token.VerifyCaptchaToken;
import io.github.lijiajia3515.cairo.auth.domain.api.open.verify_code.SendSmsVerifyCodeArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [open/api] 验证码 controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/verify_code")
@RequiredArgsConstructor
@BusinessResultBody
public class VerifyCodeOpenApiController {
	private final VerifyCodeOpenApiService verifyCodeOpenApiService;

	/**
	 * 发送短信验证码
	 *
	 * @param args 参数
	 * @return 空
	 */
	@VerifyCaptchaToken
	@PostMapping("/send_verify_code_sms")
	public Optional<String> sendVerifyCodeSms(@Validated @RequestBody SendSmsVerifyCodeArgs args) {
		verifyCodeOpenApiService.sendVerifyCodeSms(args);
		return Optional.empty();
	}
}
