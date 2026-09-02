package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业部门模板 修改状态 请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyTenantAppDepartmentTemplateStatusArgs implements Serializable {

	/**
	 * 启用状态
	 */
	@NotNull
	private Boolean enabled;
}
