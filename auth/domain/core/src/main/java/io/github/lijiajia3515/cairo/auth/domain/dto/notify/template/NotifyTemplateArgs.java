package io.github.lijiajia3515.cairo.auth.domain.dto.notify.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyTemplateArgs {
	/**
	 * 参数编码
	 */
	private String argsCode;

	/**
	 * 参数名称
	 */
	private String argsName;

	/**
	 * 数据类型
	 */
	private String dataType;

	/**
	 * 默认值
	 */
	private String defaultValue;
}
