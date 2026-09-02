package io.github.lijiajia3515.cairo.auth.modules.area;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api area feign client fallback factory
 */
@Slf4j
public class AreaClientApiFeignClientFallbackFactory implements FallbackFactory<AreaClientApiFeignClient> {
	@Override
	public AreaClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new AreaClientApiFallbackFeignClient();
	}
}
