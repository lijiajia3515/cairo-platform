package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyNotifyCategoryStatusArgs {

	/**
	 * 标识
	 */
	@NotBlank
	private String categoryId;

	/**
	 * 状态
	 */
	@NotNull
	private Boolean enabled;
}
