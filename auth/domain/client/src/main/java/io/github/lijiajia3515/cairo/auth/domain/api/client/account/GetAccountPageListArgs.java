package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取账号分页参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountPageListArgs extends AbstractPage<GetAccountPageListArgs> {

	/**
	 * 账号ID 集合
	 */
	private Collection<String> accountIds;

	/**
	 * 筛选 启用状态
	 */
	private Boolean enabled;

	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
