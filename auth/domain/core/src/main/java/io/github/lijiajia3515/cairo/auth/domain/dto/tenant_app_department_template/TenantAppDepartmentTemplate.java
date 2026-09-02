package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业部门模板
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppDepartmentTemplate implements Serializable {
	/**
	 * 上级ID
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
	 * 备注
	 */
	private String remark;

	/**
	 * 排序值
	 */
	private Long sort;
}
