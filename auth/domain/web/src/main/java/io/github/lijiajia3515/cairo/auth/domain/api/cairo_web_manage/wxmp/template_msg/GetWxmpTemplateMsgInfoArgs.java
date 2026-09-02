package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.template_msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetWxmpTemplateMsgInfoArgs implements Serializable {
	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;
}
