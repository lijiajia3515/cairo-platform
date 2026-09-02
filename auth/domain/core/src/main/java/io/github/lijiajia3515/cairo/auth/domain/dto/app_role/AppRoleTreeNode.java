package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;

import io.github.lijiajia3515.cairo.core.tree.TreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class AppRoleTreeNode implements TreeNode<String, AppRoleTreeNode>, Serializable {

	/**
	 * id
	 */
	private String roleId;

	/**
	 * parent
	 */
	private String parentId;

	/**
	 * 角色名
	 */
	private String roleName;

	/**
	 * 排序
	 */
	private Long sort;

	private List<AppRoleTreeNode> subs = new ArrayList<>(1);

	@Override
	public String id() {
		return roleId;
	}

	@Override
	public String parent() {
		return parentId;
	}

	@Override
	public List<AppRoleTreeNode> subs() {
		return subs;
	}

	@Override
	public void subs(List<AppRoleTreeNode> subs) {
		this.subs = subs;
	}
}
