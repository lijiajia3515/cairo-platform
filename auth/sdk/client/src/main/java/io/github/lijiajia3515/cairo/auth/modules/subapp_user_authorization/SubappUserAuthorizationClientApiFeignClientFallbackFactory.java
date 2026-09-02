package io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api app endpoint user feignclient fallback factory
 */
@Slf4j
public class SubappUserAuthorizationClientApiFeignClientFallbackFactory implements FallbackFactory<SubappUserAuthorizationClientApiFeignClient> {
	@Override
	public SubappUserAuthorizationClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new SubappUserAuthorizationClientApiFallbackFeignClient();
	}
}
