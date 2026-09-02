package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class EndpointClientApiFeignClientFallbackFactory implements FallbackFactory<EndpointClientApiFeignClient> {

	@Override
	public EndpointClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new EndpointClientApiFallbackFeignClient();
	}
}
