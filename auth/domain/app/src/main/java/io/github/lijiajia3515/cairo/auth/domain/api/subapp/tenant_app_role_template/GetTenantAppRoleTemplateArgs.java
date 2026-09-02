package io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template;

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

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class GetTenantAppRoleTemplateArgs extends AbstractPage<GetTenantAppRoleTemplateArgs> implements Serializable {
	/**
	 * 企业角色模板ID
	 */
	private Set<String> tenantAppRoleTemplateIds;

	/**
	 * 关键字搜索
	 */
	private String keyword;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 查询插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
