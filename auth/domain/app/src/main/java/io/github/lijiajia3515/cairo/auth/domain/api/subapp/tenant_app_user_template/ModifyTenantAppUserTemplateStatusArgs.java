package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 状态
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppUserTemplateStatusArgs implements Serializable {

	/**
	 * 企业用户模板ID
	 */
	@NotNull
	private String tenantAppUserTemplateId;

	/**
	 * 启用状态
	 */
	@NotNull
	private Boolean enabled;
}
