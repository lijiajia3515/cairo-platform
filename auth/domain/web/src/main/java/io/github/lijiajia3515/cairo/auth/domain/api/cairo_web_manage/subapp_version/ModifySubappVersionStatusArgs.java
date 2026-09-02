package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 子应用版本修改状态参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifySubappVersionStatusArgs implements Serializable {

	/**
	 * 子应用ID
	 */
	@NotNull
	@NotBlank
	private String subappId;

	/**
	 * 子应用版本
	 */
	@NotNull
	@NotBlank
	private String subappVersion;

	/**
	 * 子应用版本状态
	 */
	@NotNull
	private Boolean enabled;
}
