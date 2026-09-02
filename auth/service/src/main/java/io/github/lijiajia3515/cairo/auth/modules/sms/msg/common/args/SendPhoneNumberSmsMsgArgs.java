package io.github.lijiajia3515.cairo.auth.modules.sms.message.common.args;

import io.github.lijiajia3515.cairo.core.CoreConstants;
import jakarta.validation.constraints.NotNull;
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
public class SendPhoneNumberSmsMsgArgs {
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
	@NotNull
	private String phoneNumber;

	/**
	 * 应用ID
	 */
	@NotNull
	private String appId;

	/**
	 * 消息业务ID
	 */
	@NotNull
	private String bizId;

	/**
	 * 签名
	 */
	private String sign;

	/**
	 * 消息参数
	 */
	private Map<String, String> args;
}
