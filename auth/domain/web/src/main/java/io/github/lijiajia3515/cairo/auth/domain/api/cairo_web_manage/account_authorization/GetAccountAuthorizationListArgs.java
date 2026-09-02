package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account_authorization;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 查看账号会话参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetAccountAuthorizationListArgs extends AbstractPage<GetAccountAuthorizationListArgs> implements Serializable  {

    /**
	 * 账号id
	 */
	private String accountId;

	/**
	 * 客户端id
	 */
	private String clientId;

	/**
	 * 关键字查询
	 */
	private String keyword;

	/**
	 * 状态查询
	 */
	private String status;
}
