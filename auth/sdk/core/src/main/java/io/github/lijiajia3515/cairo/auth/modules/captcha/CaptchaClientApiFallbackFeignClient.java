package io.github.lijiajia3515.cairo.auth.modules.captcha;

import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * client-api-captcha fallback feignclient
 */
public class CaptchaClientApiFallbackFeignClient implements CaptchaClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

    @Override
    public ResponseEntity<BusinessResult<Boolean>> verifyCaptchaToken(String authorization, VerifyCaptchaTokenArgs args) {
        throw EX;
    }
}
