package io.github.lijiajia3515.cairo.auth.modules.account_sns;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-user_connect feignclient fallback factory
 */
@Slf4j
public class AccountSnsClientApiFeignClientFallbackFactory implements FallbackFactory<AccountSnsClientApiFeignClient> {
	@Override
	public AccountSnsClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AccountSnsClientApiFallbackFeignClient();
	}
}
