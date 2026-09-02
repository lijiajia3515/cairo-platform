package io.github.lijiajia3515.cairo.auth.modules.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-app feignclient fallback factory
 */

@Slf4j
public class AppClientApiFeignClientFallbackFactory implements FallbackFactory<AppClientApiFeignClient> {
	@Override
	public AppClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AppClientApiFallbackFeignClient();
	}
}
