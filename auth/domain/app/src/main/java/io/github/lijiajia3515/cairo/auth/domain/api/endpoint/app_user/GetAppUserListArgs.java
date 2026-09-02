package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user;

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

public class GetAppUserListArgs extends AbstractPage<GetAppUserListArgs> implements Serializable {

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 应用用户ID 集合
	 */
	private Collection<String> userIds;

	/**
	 * 用户状态
	 */
	private Boolean enabled;

	/**
	 * 注销状态
	 */
	private List<String> logoffStatuses;

	/**
	 * 账号ID 集合
	 */
	private Collection<String> accountIds;

	/**
	 * 角色ID 集合
	 */
	private Collection<String> roleIds;

	/**
	 * 部门ID 集合
	 */
	private Collection<String> departmentIds;

	/**
	 * 标签ID 集合
	 */
	private Collection<String> tagIds;

	/**
	 * 查询插件
	 */
	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
