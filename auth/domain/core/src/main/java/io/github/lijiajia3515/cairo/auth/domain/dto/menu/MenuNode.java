package io.github.lijiajia3515.cairo.auth.domain.dto.menu;

import io.github.lijiajia3515.cairo.core.tree.TreeNode2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 树节点 - 资源
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MenuNode implements TreeNode2<String, MenuNode>, Serializable {
	/**
	 * 菜单Id
	 */
	private String menuId;

	/**
	 * 上级id
	 */
	private String parentId;

	/**
	 * 菜单名称
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
	 * 菜单icon
	 */
	private String icon;

	/**
	 * 是否隐藏
	 */
	private Boolean hiddenMenu;

	/**
	 * 标签，非必填
	 */
	private List<String> tags;

	/**
	 * 是否勾选
	 */
	private Boolean isSelected;

	/**
	 * 左值
	 */
	private Integer leftNo;

	/**
	 * 右值
	 */
	private Integer rightNo;

	/**
	 * 层级
	 */
	private Integer depth;

	@Builder.Default
	private List<MenuNode> menus = new ArrayList<>(1);

	@Builder.Default
	private List<Permission> permissions = new ArrayList<>(1);

	@Override
	public String id() {
		return menuId;
	}

	@Override
	public String parentId() {
		return parentId;
	}

	@Override
	public int depth() {
		return depth;
	}

	@Override
	public int leftNo() {
		return leftNo;
	}

	@Override
	public int rightNo() {
		return rightNo;
	}


	@Override
	public List<MenuNode> subs() {
		return menus;
	}

	@Override
	public void subs(List<MenuNode> subs) {
		this.menus = subs;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Permission {
		/**
		 * 元素id
		 */
		private String permissionId;

		/**
		 * 元素显示名称
		 */
		private String permissionName;

		/**
		 * 接口所需要的权限标识
		 */
		private Set<String> authorities;

		/**
		 * 类型（read=读，write=写，operator=操作）选填
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

		/**
		 * 权限图标
		 */
		private String icon;

		/**
		 * 是否勾选
		 */
		private Boolean isSelected;
	}
}
