package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除企业角色模板权限参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteTenantAppRoleTemplatePermissionArgs implements Serializable {

	/**
	 * 企业角色模板ID
	 */
	@NotNull
	@NotBlank
	private String tenantAppRoleTemplateId;


	/**
	 * 终端ID
	 */
	@NotNull
	@NotBlank
	private String endpointId;

	/**
	 * subappId
	 */
	@NotNull
	private String subappId;

	/**
	 * subappVersion
	 */
	@NotNull
	private String subappVersion;
}
