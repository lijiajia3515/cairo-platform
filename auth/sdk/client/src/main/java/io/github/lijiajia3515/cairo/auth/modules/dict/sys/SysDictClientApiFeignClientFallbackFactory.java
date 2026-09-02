package io.github.lijiajia3515.cairo.auth.modules.dict.sys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api system dict feign client fallback factory
 */
@Slf4j
public class SysDictClientApiFeignClientFallbackFactory implements FallbackFactory<SysDictClientApiFeignClient> {

	@Override
	public SysDictClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new SysDictClientApiFallbackFeignClient();
	}

}
