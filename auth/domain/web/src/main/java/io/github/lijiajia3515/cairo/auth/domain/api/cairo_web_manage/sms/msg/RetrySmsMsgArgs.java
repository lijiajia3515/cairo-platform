package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RetrySmsMsgArgs {

	/**
	 * 消息ID
	 */
	private String msgId;
}
