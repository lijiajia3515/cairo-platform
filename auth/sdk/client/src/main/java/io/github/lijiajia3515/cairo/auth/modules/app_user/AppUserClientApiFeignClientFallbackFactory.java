package io.github.lijiajia3515.cairo.auth.modules.app_user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-user feignclient fallback factory
 */
@Slf4j
public class AppUserClientApiFeignClientFallbackFactory implements FallbackFactory<AppUserClientApiFeignClient> {
	@Override
	public AppUserClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AppUserClientApiFallbackFeignClient();
	}
}
