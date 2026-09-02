package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;


import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class WxmpSendMsgClientApiFallbackFeignClient implements WxmpSendMsgClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-微信公众号发送消息子应用故障");

	@Override
	public ResponseEntity<BusinessResult<String>> sendMsgByAppUser(String authorization, SendWxmpMsgByArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> sendMsg(String authorization, SendWxmpMsgArgs args) {
		throw EX;
	}
}
