package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_app;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 修改企业应用状态
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppStatusArgs implements Serializable {

	/**
	 * tenant id
	 */
	@NotBlank
	private String tenantId;

	/**
	 * app id
	 */
	@NotBlank
	private String appId;


	/**
	 * 状态
	 */
	private Boolean enabled;
}
