package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-subapp feignclient fallback factory
 */
@Slf4j
public class TenantSubappApiClientFeignClientFallbackFactory implements FallbackFactory<TenantSubappApiClientFeignClient> {
	@Override
	public TenantSubappApiClientFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantSubappApiClientFallbackFeignClient();
	}
}
