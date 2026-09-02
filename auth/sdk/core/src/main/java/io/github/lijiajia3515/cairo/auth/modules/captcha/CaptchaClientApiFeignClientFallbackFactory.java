package io.github.lijiajia3515.cairo.auth.modules.captcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-captcha feignclient fallback factory
 */
@Slf4j
public class CaptchaClientApiFeignClientFallbackFactory implements FallbackFactory<CaptchaClientApiFeignClient> {
	@Override
	public CaptchaClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new CaptchaClientApiFallbackFeignClient();
	}
}
