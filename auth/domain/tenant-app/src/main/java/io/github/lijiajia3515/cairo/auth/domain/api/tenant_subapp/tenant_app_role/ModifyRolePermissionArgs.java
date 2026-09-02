package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role;

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
 * 修改角色权限参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyRolePermissionArgs implements Serializable {

	/**
	 * 角色ID
	 */
	@NotNull
	@NotBlank
	private String roleId;


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

	/**
	 * 功能权限集合
	 */
	@NotNull
	private List<String> permissionIds;
}
