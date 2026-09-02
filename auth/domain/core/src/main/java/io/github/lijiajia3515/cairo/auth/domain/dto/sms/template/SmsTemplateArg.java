package io.github.lijiajia3515.cairo.auth.domain.dto.sms.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsTemplateArg {
	/**
	 * 参数编码
	 */
	private String argCode;
	/**
	 * 模板参数名称
	 */
	private String argName;

	/**
	 * 模板参数类型
	 */
	private String argType;
	/**
	 * 模板参数编码
	 */
	private String templateArgCode;
}
