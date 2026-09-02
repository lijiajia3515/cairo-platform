package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取企业角色子应用版本参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTenantAppRoleTemplateSubappVersionArgs {

	/**
	 * 企业角色模板ID
	 */
	@NotNull
	private String tenantAppRoleTemplateId;


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
