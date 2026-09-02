package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除企业部门模板请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteTenantAppDepartmentTemplateArgs implements Serializable {
	/**
	 * 企业部门模板ID
	 */
	@NotNull
	@NotEmpty
	private String tenantAppDepartmentTemplateId;
}
