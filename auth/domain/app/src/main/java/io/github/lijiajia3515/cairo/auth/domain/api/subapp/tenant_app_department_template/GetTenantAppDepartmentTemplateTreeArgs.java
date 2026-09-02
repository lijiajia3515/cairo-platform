package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取企业部门模板树请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantAppDepartmentTemplateTreeArgs implements Serializable {
	/**
	 * 上级ID
	 */
	private String parentId;
}
