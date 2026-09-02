package io.github.lijiajia3515.cairo.auth.modules.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class TenantAppFileClientApiFeignClientFallbackFactory implements FallbackFactory<TenantAppFileClientApiFeignClient> {
	@Override
	public TenantAppFileClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new TenantAppFileClientApiFallbackFeignClient();
	}
}
