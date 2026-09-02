package io.github.lijiajia3515.cairo.auth.modules.wxmp.mass_msg;

import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;

public interface WxmpMassMsgClientApiService {

	/**
	 * 公众号群发 发送
	 *
	 * @param args 参数
	 * @return WxmpMassMsgResult
	 */
	WxmpMassMsgResult sendWxmpMassMsg(SendWxmpMassMsgArgs args);

	/**
	 * 公众号群发 删除
	 *
	 * @param args 参数
	 */
	String deleteWxmpMassMsg(DeleteWxmpMassMsgArgs args);
}
