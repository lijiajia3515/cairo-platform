package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取用户 参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GetTenantAppUserArgs extends AbstractPage<GetTenantAppUserArgs> implements Serializable {

	/**
	 * 租户ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 用户ID集合
	 */
	private Collection<String> userIds;

	/**
	 * 账号ID集合
	 */
	private Collection<String> accountIds;

	/**
	 * 角色编码
	 */
	private Collection<String> roleIds;

	/**
	 * 用户状态
	 */
	private Collection<Boolean> statuses;

	/**
	 * 部门ID 集合
	 */
	private Collection<String> departmentIds;


	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
