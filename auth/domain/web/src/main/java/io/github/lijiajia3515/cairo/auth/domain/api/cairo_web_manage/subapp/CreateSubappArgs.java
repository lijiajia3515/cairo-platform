package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 *  创建 子应用 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateSubappArgs implements Serializable {
	/**
	 * 子应用ID
	 */
	@NotNull
	@NotBlank
	private String subappId;

	/**
	 * 子应用名称
	 */
	@NotNull
	@NotBlank
	private String subappName;

	/**
	 * 子应用图标
	 */
	private String subappIcon;

	/**
	 * 准入范围
	 */
	private String scope;

	/**
	 * 启用状态
	 */
	@Builder.Default
	private boolean enabled = false;
}
