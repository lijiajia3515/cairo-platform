package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取应用角色子应用版本参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppRoleSubappVersionArgs {

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

}
