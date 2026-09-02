package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department;

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

/**
 * 树节点 - 部门
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TreeNodeTenantAppDepartment implements TreeNode2<String, TreeNodeTenantAppDepartment>, Serializable {
	/**
	 * 父级 ID
	 */
	private String parentId;

	/**
	 * 根节点
	 */
	private boolean root;

	/**
	 * 部门ID
	 */
	private String departmentId;


	/**
	 * 部门名称
	 */
	private String departmentName;

	/**
	 * 启用
	 */
	private Boolean enabled;

	/**
	 * 左值
	 */
	private Integer leftNo;

	/**
	 * 右值
	 */
	private Integer rightNo;

	/**
	 * 深度
	 */
	private Integer depth;

	/**
	 * 子集
	 */
	@Builder.Default
	private List<TreeNodeTenantAppDepartment> subs = new ArrayList<>();

	@Override
	public String id() {
		return departmentId;
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
	public List<TreeNodeTenantAppDepartment> subs() {
		return subs;
	}

	@Override
	public void subs(List<TreeNodeTenantAppDepartment> subs) {
		this.subs = subs;
	}
}
