package io.github.lijiajia3515.cairo.auth.modules.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-account feignclient fallback factory
 */
@Slf4j
public class AccountClientApiFeignClientFallbackFactory implements FallbackFactory<AccountClientApiFeignClient> {
	@Override
	public AccountClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AccountClientApiFallbackFeignClient();
	}
}
