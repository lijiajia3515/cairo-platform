package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建企业部门模板请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateTenantAppDepartmentTemplateArgs implements Serializable {
	/**
	 * 父级ID
	 */
	private String parentId;

	/**
	 * 排序id之前
	 */
	private String beforeId;

	/**
	 * 名称
	 */
	@NotNull
	private String tenantAppDepartmentTemplateName;

	/**
	 * 备注
	 */
	private String remark;
}
