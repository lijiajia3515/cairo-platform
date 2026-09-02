package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 企业部门模板 路径模式 对象
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PathTenantAppDepartmentTemplate implements Serializable {

	/**
	 * 企业部门模板ID，层级级由低到高
	 */
	private List<String> tenantAppDepartmentTemplateIds;

	/**
	 * 部门名称，层级由低到高
	 */
	private List<String> tenantAppDepartmentTemplateNames;

	/**
	 * 层级
	 */
	private Integer depth;
}
