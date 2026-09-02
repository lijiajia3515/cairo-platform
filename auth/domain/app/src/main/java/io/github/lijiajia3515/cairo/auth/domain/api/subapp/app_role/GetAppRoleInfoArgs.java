package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取应用角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppRoleInfoArgs {
	@NotNull
	private String roleId;
}
