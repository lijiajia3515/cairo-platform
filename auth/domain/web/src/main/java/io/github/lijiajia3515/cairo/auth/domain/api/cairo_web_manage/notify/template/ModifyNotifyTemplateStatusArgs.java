package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改通知消息模板状态 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyNotifyTemplateStatusArgs implements Serializable {

	/**
	 * 模板ID
	 */
	@NotNull
	private String templateId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
