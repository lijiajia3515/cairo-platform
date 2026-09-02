package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template;

import jakarta.validation.constraints.NotNull;
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
	@NotNull
	private String argsCode;

	/**
	 * 参数名称
	 */
	@NotNull
	private String argsName;

	/**
	 * 数据类型
	 */
	@NotNull
	private String dataType;

	/**
	 * 默认值
	 */
	private String defaultValue;
}
