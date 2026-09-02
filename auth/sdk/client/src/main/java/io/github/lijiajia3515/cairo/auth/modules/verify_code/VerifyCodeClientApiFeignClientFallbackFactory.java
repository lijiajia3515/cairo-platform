package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import io.github.lijiajia3515.cairo.auth.modules.account_sns.AccountSnsClientApiFallbackFeignClient;
import io.github.lijiajia3515.cairo.auth.modules.account_sns.AccountSnsClientApiFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-user_connect feignclient fallback factory
 */
@Slf4j
public class VerifyCodeClientApiFeignClientFallbackFactory implements FallbackFactory<VerifyCodeClientApiFeignClient> {
	@Override
	public VerifyCodeClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new VerifyCodeClientApiFallbackFeignClient();
	}
}
