package io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;
import org.springframework.http.ResponseEntity;

public class WxmpMassMsgClientApiFallbackFeignClient implements WxmpMassMsgClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-微信公总号推文子应用故障");

	@Override
	public ResponseEntity<BusinessResult<WxmpMassMsgResult>> sendWxmpMassMsg(String authorization, SendWxmpMassMsgArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<String>> deleteWxmpMassMsg(String authorization, DeleteWxmpMassMsgArgs args) {
		throw EX;
	}
}
