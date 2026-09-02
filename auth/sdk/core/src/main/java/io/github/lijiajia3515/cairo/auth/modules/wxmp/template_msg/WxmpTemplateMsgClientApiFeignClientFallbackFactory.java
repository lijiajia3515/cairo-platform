package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg.WxmpSendMsgClientApiFallbackFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class WxmpTemplateMsgClientApiFeignClientFallbackFactory implements FallbackFactory<WxmpTemplateMsgClientApiFeignClient> {
	@Override
	public WxmpTemplateMsgClientApiFeignClient create(Throwable cause) {
		log.info("服务异常原因: ", cause);
		return new WxmpTemplateMsgClientApiFallbackFeignClient();
	}
}
