package io.github.lijiajia3515.cairo.auth.domain.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PathMenu implements Serializable {
	/**
	 * id
	 */
	private String menuId;

	/**
	 * 所有菜单ID，从小到大
	 */
	private List<String> menuIds;

	/**
	 * 所有菜单名称，从小到大
	 */
	private List<String> menuNames;

	/**
	 * 前端路径/页面地址外部地址
	 */
	private String path;

	/**
	 * 组件名
	 */
	private String component;

	/**
	 * icon
	 */
	private String icon;
}
