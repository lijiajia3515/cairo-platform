package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api tenant app subapp user authorization feignclient fallback factory
 */
@Slf4j
public class TenantSubappUserAuthorizationClientApiFeignClientFallbackFactory implements FallbackFactory<TenantSubappUserAuthorizationClientApiFeignClient> {
	@Override
	public TenantSubappUserAuthorizationClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantSubappUserAuthorizationClientApiFallbackFeignClient();
	}
}
