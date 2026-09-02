package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template;

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
 * 树节点 - 企业部门模板
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TreeNodeTenantAppDepartmentTemplate implements TreeNode2<String, TreeNodeTenantAppDepartmentTemplate>, Serializable {

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
	private String tenantAppDepartmentTemplateId;


	/**
	 * 名称
	 */
	private String tenantAppDepartmentTemplateName;

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
	private List<TreeNodeTenantAppDepartmentTemplate> subs = new ArrayList<>();

	@Override
	public String id() {
		return tenantAppDepartmentTemplateId;
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
	public List<TreeNodeTenantAppDepartmentTemplate> subs() {
		return subs;
	}

	@Override
	public void subs(List<TreeNodeTenantAppDepartmentTemplate> subs) {
		this.subs = subs;
	}
}
