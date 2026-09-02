package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sms.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSmsTemplateInfoArgs implements Serializable {
	/**
	 * 业务ID
	 */
	@NotNull
	private String bizId;
}
