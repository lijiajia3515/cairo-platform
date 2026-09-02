package io.github.lijiajia3515.cairo.auth.modules.app_doc.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * tenant doc client api feign client fallback factory
 */
@Slf4j
public class AppDocClientApiFeignClientFallbackFactory implements FallbackFactory<AppDocClientApiFeignClient> {
	@Override
	public AppDocClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new AppDocClientApiFallbackFeignClient();
	}
}
