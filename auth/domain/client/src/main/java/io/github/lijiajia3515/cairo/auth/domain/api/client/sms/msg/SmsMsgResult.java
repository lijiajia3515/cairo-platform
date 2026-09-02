package io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信消息发送结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMsgResult {
	/**
	 * 消息ID
	 */
	private String msgId;
}
