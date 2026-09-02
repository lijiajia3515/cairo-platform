package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTemplateMsgArgs {

	/**
	 * 业务ID
	 */
	@NotNull
	@NotBlank
	private String bizId;
}
