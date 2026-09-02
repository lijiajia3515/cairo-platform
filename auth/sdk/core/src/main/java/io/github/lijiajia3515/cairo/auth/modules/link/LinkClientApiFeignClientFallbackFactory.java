package io.github.lijiajia3515.cairo.auth.modules.link;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api imgproxy feign client fallback factory
 */
@Slf4j
public class LinkClientApiFeignClientFallbackFactory implements FallbackFactory<LinkClientApiFeignClient> {
	@Override
	public LinkClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new LinkClientApiFallbackFeignClient();
	}
}
