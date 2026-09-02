package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class SubappVersionClientApiFeignClientFallbackFactory implements FallbackFactory<SubappVersionClientApiFeignClient> {

	@Override
	public SubappVersionClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因：", cause);
		return new SubappVersionClientApiFallbackFeignClient();
	}
}
