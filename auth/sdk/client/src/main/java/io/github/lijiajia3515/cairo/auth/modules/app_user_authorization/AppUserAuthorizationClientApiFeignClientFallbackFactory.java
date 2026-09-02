package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api app endpoint user feignclient fallback factory
 */
@Slf4j
public class AppUserAuthorizationClientApiFeignClientFallbackFactory implements FallbackFactory<AppUserAuthorizationClientApiFeignClient> {
	@Override
	public AppUserAuthorizationClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new AppUserAuthorizationClientApiFallbackFeignClient();
	}
}
