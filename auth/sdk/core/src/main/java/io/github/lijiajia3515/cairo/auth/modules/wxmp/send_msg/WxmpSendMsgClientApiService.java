package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;

import java.util.Optional;

public interface WxmpSendMsgClientApiService {

	/**
	 * 应用级用户发送微信消息
	 *需要权限 wxmp_message:send_msg | wxmp_template_msg:all
	 *
	 * @param args 参数
	 * @return empty
	 */
	String sendMsgByAppUser(SendWxmpMsgByArgs args);

	/**
	 * 发送微信消息
	 *需要权限 wxmp_message:send_msg | wxmp_template_msg:all
	 *
	 * @param args 参数
	 * @return empty
	 */
	Optional<String> sendMsg(SendWxmpMsgArgs args);

}
