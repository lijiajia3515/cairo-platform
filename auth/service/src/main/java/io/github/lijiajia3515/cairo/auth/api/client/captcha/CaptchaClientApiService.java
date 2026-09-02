package io.github.lijiajia3515.cairo.auth.api.client.captcha;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaTokenService;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.VerifyCaptchaTokenArgs;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * [client/api] captcha service
 */
@Slf4j
@Validated
@Component
public class CaptchaClientApiService {
	private final CaptchaTokenService captchaTokenService;

	public CaptchaClientApiService(CaptchaTokenService captchaTokenService) {
		this.captchaTokenService = captchaTokenService;
	}

	/**
	 * 验证验证码token
	 *
	 * @param token token
	 * @param ip    客户端ip
	 * @return 是否成功
	 */
	@NewSpan
	@BizLog(
		bizId = "captcha:verify_captcha_token",
		scope = "write",
		params = {
			@BizLog.Param(key = "token", value = "#token"),
			@BizLog.Param(key = "ip", value = "#ip"),
		}
	)
	public boolean verifyCaptchaToken(@Valid @NotNull String token, @Valid @NotNull String ip) {
		return captchaTokenService.verifyToken(VerifyCaptchaTokenArgs.builder()
			.token(token)
			.ip(ip)
			.build());
	}
}
