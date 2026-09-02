package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetRoleInfoArgs {
	@NotNull
	private String roleId;
}
