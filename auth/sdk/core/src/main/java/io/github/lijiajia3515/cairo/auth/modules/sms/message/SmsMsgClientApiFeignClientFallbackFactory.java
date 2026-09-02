package io.github.lijiajia3515.cairo.auth.modules.sms.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api sms message feign client fallback factory
 */
@Slf4j
public class SmsMsgClientApiFeignClientFallbackFactory implements FallbackFactory<SmsMsgClientApiFeignClient> {
	@Override
	public SmsMsgClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new SmsMsgClientApiFallbackFeignClient();
	}
}
