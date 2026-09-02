package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改短信模板状态参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifySmsTemplateStatusArgs implements Serializable {

	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


}
