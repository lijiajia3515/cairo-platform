package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import io.github.lijiajia3515.cairo.core.tree.TreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TenantAppRoleTemplateTreeNode implements TreeNode<String, TenantAppRoleTemplateTreeNode>, Serializable {

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

	private List<TenantAppRoleTemplateTreeNode> subs = new ArrayList<>(1);

	@Override
	public String id() {
		return roleId;
	}

	@Override
	public String parent() {
		return parentId;
	}

	@Override
	public List<TenantAppRoleTemplateTreeNode> subs() {
		return subs;
	}

	@Override
	public void subs(List<TenantAppRoleTemplateTreeNode> subs) {
		this.subs = subs;
	}
}
