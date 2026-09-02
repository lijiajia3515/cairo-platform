package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 创建角色参数
 */
@Data
@Accessors(chain = true)
public class CreateRoleArgs implements Serializable {
	/**
	 * 角色ID
	 */
	private String roleId;

	/**
	 * 名称
	 */
	@NotNull
	private String roleName;

	/**
	 * 备注
	 */
	private String remark;
}
