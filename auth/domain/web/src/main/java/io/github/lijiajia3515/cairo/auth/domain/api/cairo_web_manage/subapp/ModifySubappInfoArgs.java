package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改 子应用信息 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifySubappInfoArgs implements Serializable {

	/**
	 * ID
	 */
	@NotNull
	@NotBlank
	private String id;

	/**
	 * 子应用ID
	 */
	@NotNull
	@NotBlank
	private String subappId;

	/**
	 * 子应用名称
	 */
	private String subappName;

	/**
	 * 子应用图标
	 */
	private String subappIcon;

	/**
	 * 准入范围
	 */
	private String scope;

}
