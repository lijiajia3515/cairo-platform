package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 创建菜单参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateMenuArgs implements Serializable {

	/**
	 * 上级id
	 */
	@NotNull
	private String parentId;

	/**
	 * 排序id之前 非必填
	 */
	private String beforeId;
	/**
	 * 菜单名称
	 */
	@NotNull
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
	 * 接口权限标识
	 */
	private String icon;

	/**
	 * 是否隐藏
	 */
	private boolean hiddenMenu;

	/**
	 * 标签
	 */
	private List<String> tags;

}
