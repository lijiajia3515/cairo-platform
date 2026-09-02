package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取企业角色模板信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTenantAppRoleTemplateInfoArgs {
	@NotNull
	private String tenantAppRoleTemplateId;
}
