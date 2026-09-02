package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;


import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import org.springframework.http.ResponseEntity;


public class WxmpTemplateMsgClientApiFallbackFeignClient implements WxmpTemplateMsgClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-微信公众号子应用消息故障");

	@Override
	public ResponseEntity<BusinessResult<WxmpTemplateMsg>> getWxmpTemplateMsg(String authorization, GetTemplateMsgArgs args) {
		throw EX;
	}
}
