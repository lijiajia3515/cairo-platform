package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 移动部门参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MoveAppDepartmentArgs implements Serializable {

	/**
	 * 部门ID
	 */
	@NotNull
	private String moveId;

	/**
	 * 移动到菜单里
	 */
	@NotNull
	private String parentId;

	/**
	 * 移动到菜单ID
	 */
	private String beforeId;
}
