package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 基本应用角色信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppRole implements Serializable {
	/**
	 * 角色标识
	 */
	private String roleId;

	/**
	 * 名称
	 */
	private String roleName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 启用状态
	 */
	private Boolean enabled;
}
