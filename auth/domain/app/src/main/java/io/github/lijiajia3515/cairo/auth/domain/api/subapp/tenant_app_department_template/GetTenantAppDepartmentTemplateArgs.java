package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template;


import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 获取企业部门模板参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetTenantAppDepartmentTemplateArgs extends AbstractPage<GetTenantAppDepartmentTemplateArgs> implements Serializable {

	/**
	 * 上级ID
	 */
	private String parentId;

	/**
	 * 企业部门模板ID
	 */
	private Set<String> tenantAppDepartmentTemplateIds;


	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
