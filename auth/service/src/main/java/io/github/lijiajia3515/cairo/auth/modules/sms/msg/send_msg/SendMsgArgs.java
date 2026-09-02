package io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg;

import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
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
public class SendMsgArgs {
	/**
	 * 消息ID，每次发送不重复
	 */
	@Builder.Default
	private String msgId = CoreConstants.SNOWFLAKE.nextIdStr();

	/**
	 * 时间
	 */
	@Builder.Default
	private LocalDateTime time = LocalDateTime.now();
	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 业务ID
	 */
	private String bizId;

	/**
	 * 业务签名
	 */
	private String bizSign;

	/**
	 * 业务参数
	 */
	private Map<String, String> bizArgs;

	/**
	 * 短信模板
	 */
	private SmsTemplate smsTemplate;
}
