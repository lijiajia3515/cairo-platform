package io.github.lijiajia3515.cairo.auth.modules.sns_provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api-sns-provider feignclient fallback factory
 */
@Slf4j
public class SnsProviderClientApiFeignClientFallbackFactory implements FallbackFactory<SnsProviderClientApiFeignClient> {
	@Override
	public SnsProviderClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new SnsProviderClientApiFallbackFeignClient();
	}
}
