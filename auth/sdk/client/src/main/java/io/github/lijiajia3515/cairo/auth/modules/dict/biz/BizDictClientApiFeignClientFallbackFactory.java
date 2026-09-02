package io.github.lijiajia3515.cairo.auth.modules.dict.biz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * client-api biz dict feign client fallback factory
 */
@Slf4j
public class BizDictClientApiFeignClientFallbackFactory implements FallbackFactory<BizDictClientApiFeignClient> {

	@Override
	public BizDictClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new BizDictClientApiFallbackFeignClient();
	}

}
