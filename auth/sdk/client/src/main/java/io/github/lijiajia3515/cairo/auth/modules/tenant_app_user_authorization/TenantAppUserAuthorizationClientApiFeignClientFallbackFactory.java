package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-account feignclient fallback factory
 */
@Slf4j
public class TenantAppUserAuthorizationClientApiFeignClientFallbackFactory implements FallbackFactory<TenantAppUserAuthorizationClientApiFeignClient> {
	@Override
	public TenantAppUserAuthorizationClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppUserAuthorizationClientApiFallbackFeignClient();
	}
}
