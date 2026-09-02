package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_subapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改企业子应用状态 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantSubappStatusArgs implements Serializable {

	/**
	 * 企业ID
	 */
	@NotBlank
	private String tenantId;

	/**
	 * 应用ID
	 */
	@NotBlank
	private String appId;

	/**
	 * 终端ID
	 */
	@NotBlank
	private String endpointId;


	/**
	 * 终端ID
	 */
	@NotNull
	private String subappId;


	/**
	 * 状态
	 */
	private Boolean enabled;
}
