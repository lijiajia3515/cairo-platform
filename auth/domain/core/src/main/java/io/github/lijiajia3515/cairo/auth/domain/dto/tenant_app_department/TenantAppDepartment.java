package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 部门
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppDepartment implements Serializable {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 上级ID
	 */
	private String parentId;

	/**
	 * 是否根节点
	 */
	private boolean root;

	/**
	 * 部门ID
	 */
	private String departmentId;

	/**
	 * 名称
	 */
	private String departmentName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 排序值
	 */
	private Long sort;
}
