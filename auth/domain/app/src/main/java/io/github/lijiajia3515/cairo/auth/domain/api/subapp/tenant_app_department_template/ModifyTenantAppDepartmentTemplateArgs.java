package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业部门模板 修改 请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppDepartmentTemplateArgs implements Serializable {
	/**
	 * 企业部门模板 id
	 */
	@NotNull
	private String tenantAppDepartmentTemplateId;
	/**
	 * 上级 id
	 */
	private String parentId;
	/**
	 * 企业部门名称
	 */
	private String tenantAppDepartmentTemplateName;
	/**
	 * 备注
	 */
	private String remark;
}
