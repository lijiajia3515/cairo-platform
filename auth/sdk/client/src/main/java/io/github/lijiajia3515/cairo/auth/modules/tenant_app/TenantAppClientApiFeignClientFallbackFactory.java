package io.github.lijiajia3515.cairo.auth.modules.tenant_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class TenantAppClientApiFeignClientFallbackFactory implements FallbackFactory<TenantAppClientApiFeignClient> {
	@Override
	public TenantAppClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new TenantAppClientApiFallbackFeignClient();
	}
}
