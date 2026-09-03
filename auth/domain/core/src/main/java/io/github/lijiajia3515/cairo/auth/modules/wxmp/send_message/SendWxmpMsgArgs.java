package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendWxmpMsgArgs {
	/**
	 * 消息ID，每次发送不重复
	 */
	@Builder.Default
	private String msgId = CoreConstants.nextIdStr();

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 业务ID
	 */
	private String bizId;

	/**
	 * 微信用户id
	 */
	private String openId;

	/**
	 * 业务参数
	 */
	private Map<String, SendWxmpMsgByArgs.MessageContent> params;


	/**
	 * 跳转链接
	 */
	private String jumpUrl;

	/**
	 * 微信模板
	 */
	private WxmpTemplateMsg wxmpTemplateMsg;

	/**
	 * 三方认证id
	 */
	private String snsProviderId;


	/**
	 * 来源
	 */
	private String source;


	/**
	 * 时间
	 */
	@Builder.Default
	private LocalDateTime time = LocalDateTime.now();
}
