package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * 功能权限保存参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreatePermissionArgs implements Serializable {

	/**
	 * 菜单ID
	 */
	private String menuId;

	/**
	 * 功能权限ID
	 */
	@NotNull
	private String permissionId;

	/**
	 * 功能权限名称
	 */
	@NotNull
	private String permissionName;

	/**
	 * icon
	 */
	private String icon;

	/**
	 * 接口权限标识
	 */
	private Set<String> authorities;

	/**
	 * 类型
	 */
	private String type;

	/**
	 * 是否默认拥有
	 */
	private Boolean defaultPermission;

	/**
	 * 是否隐藏
	 */
	private Boolean hiddenPermission;

	/**
	 * 排序值
	 */
	private Long sort;
}
