package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg_record;

import jakarta.validation.constraints.NotBlank;
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
public class RetryWxmpTemplateMsgRecordArgs  {

	/**
	 * 消息ID
	 */
	@NotBlank
	private String msgId;
}
