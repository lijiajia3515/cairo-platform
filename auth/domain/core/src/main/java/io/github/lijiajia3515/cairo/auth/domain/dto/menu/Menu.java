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
public class Menu implements Serializable {
	/**
	 * id
	 */
	private String menuId;

	/**
	 * 上级id
	 */
	private String parentId;

	/**
	 * 名称
	 */
	private String menuName;

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


	/**
	 * 是否隐藏
	 */
	private Boolean hiddenMenu;

	/**
	 * 标签
	 */
	private List<String> tags;
}
