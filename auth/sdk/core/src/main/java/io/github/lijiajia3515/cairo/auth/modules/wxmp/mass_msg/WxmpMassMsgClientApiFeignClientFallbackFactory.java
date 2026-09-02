package io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class WxmpMassMsgClientApiFeignClientFallbackFactory implements FallbackFactory<WxmpMassMsgClientApiFeignClient> {
	@Override
	public WxmpMassMsgClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new WxmpMassMsgClientApiFallbackFeignClient();
	}
}
