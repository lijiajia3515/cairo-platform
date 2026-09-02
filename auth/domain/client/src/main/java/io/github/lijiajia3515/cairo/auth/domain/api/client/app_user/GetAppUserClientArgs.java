package io.github.lijiajia3515.cairo.auth.domain.api.client.app_user;

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
public class GetAppUserClientArgs extends AbstractPage<GetAppUserClientArgs> implements Serializable {

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

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
