package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-user feignclient fallback factory
 */
@Slf4j
public class TenantAppUserClientApiFeignClientFallbackFactory implements FallbackFactory<TenantAppUserClientApiFeignClient> {
	@Override
	public TenantAppUserClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppUserClientApiFallbackFeignClient();
	}
}
