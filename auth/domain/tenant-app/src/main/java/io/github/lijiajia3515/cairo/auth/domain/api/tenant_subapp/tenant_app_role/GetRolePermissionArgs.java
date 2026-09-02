package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取角色权限参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetRolePermissionArgs {

	/**
	 * 角色ID
	 */
	@NotNull
	private String roleId;

	/**
	 * 终端ID
	 */
	@NotNull
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
