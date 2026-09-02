package io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendPhoneNumberSmsMsgArgs {

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
