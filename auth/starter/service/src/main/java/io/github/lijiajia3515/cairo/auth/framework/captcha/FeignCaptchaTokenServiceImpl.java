package io.github.lijiajia3515.cairo.auth.framework.captcha;

import io.github.lijiajia3515.cairo.auth.modules.captcha.CaptchaClientApiService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaToken;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.CaptchaTokenService;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.StoreCaptchaTokenArgs;

import java.util.Optional;

public class FeignCaptchaTokenServiceImpl implements CaptchaTokenService {
    private final CaptchaClientApiService captchaClientApiService;

    public FeignCaptchaTokenServiceImpl(CaptchaClientApiService captchaClientApiService) {
        this.captchaClientApiService = captchaClientApiService;
    }

    @Override
    public CaptchaToken storeToken(StoreCaptchaTokenArgs args) {
        return null;
    }

    @Override
    public boolean verifyToken(io.github.lijiajia3515.cairo.auth.modules.captcha.token.VerifyCaptchaTokenArgs args) {
		Boolean aBoolean = captchaClientApiService.verifyCaptchaToken(
			VerifyCaptchaTokenArgs.builder()
				.captchaToken(args.getToken())
				.clientIp(args.getIp())
				.build()
		);
		return Optional.ofNullable(aBoolean).orElse(false);
    }
}
