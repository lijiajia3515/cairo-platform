package io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.account;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
	 * 关键字
	 */
	private String keyword;
}
