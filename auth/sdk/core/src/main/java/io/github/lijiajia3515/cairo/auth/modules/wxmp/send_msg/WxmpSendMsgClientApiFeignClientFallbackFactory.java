package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class WxmpSendMsgClientApiFeignClientFallbackFactory implements FallbackFactory<WxmpSendMsgClientApiFeignClient> {
	@Override
	public WxmpSendMsgClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new WxmpSendMsgClientApiFallbackFeignClient();
	}
}
