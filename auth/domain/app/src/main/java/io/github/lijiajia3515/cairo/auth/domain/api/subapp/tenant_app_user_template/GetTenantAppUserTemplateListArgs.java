package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class GetTenantAppUserTemplateListArgs extends AbstractPage<GetTenantAppUserTemplateListArgs> implements Serializable {

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 企业用户模板ID 集合
	 */
	private Collection<String> tenantAppUserTemplateIds;

	/**
	 * 用户状态
	 */
	private Boolean enabled;


	/**
	 * 账号ID 集合
	 */
	private Collection<String> accountIds;

	/**
	 * 角色
	 */
	private List<String> tenantAppRoleTemplateIds;

	/**
	 * 部门
	 */
	private List<String> tenantAppDepartmentTemplateIds;



	/**
	 * 查询插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
