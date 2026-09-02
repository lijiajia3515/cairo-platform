package io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * tenant doc client api feign client fallback factory
 */
@Slf4j
public class TenantAppDocClientApiFeignClientFallbackFactory implements FallbackFactory<TenantAppDocClientApiFeignClient> {
	@Override
	public TenantAppDocClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new TenantAppDocClientApiFallbackFeignClient();
	}
}
