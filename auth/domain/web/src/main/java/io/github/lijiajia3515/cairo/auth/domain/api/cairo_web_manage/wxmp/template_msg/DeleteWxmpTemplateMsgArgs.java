package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 删除微信模板参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteWxmpTemplateMsgArgs implements Serializable {

	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;

}
