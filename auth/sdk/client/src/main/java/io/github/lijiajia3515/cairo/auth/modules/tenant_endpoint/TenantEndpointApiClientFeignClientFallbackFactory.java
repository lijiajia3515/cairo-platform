package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-endpoint feignclient fallback factory
 */
@Slf4j
public class TenantEndpointApiClientFeignClientFallbackFactory implements FallbackFactory<TenantEndpointApiClientFeignClient> {
	@Override
	public TenantEndpointApiClientFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantEndpointApiClientFallbackFeignClient();
	}
}
