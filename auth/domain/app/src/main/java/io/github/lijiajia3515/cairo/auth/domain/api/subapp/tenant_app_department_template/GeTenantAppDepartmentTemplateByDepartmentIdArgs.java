package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取企业部门模板请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeTenantAppDepartmentTemplateByDepartmentIdArgs implements Serializable {

	/**
	 * 企业部门模板 id
	 */
	@NotNull
	private String tenantAppDepartmentTemplateId;
}
