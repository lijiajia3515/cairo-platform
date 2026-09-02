package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyClientStatusArgs {
	/**
	 * id
	 */
	@NotNull
	private String id;

	/**
	 * 启用/禁用
	 */
	private Boolean enabled;
}
