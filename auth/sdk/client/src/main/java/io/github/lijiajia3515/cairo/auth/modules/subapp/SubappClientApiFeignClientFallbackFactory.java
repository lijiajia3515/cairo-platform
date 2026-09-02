package io.github.lijiajia3515.cairo.auth.modules.subapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class SubappClientApiFeignClientFallbackFactory implements FallbackFactory<SubappClientApiFeignClient> {

	@Override
	public SubappClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new SubappClientApiFallbackFeignClient();
	}
}
