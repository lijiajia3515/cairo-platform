package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取通知消息模板信息 参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetNotifyTemplateInfoArgs implements Serializable {
	/**
	 * 模板ID
	 */
	@NotNull
	private String templateId;
}
