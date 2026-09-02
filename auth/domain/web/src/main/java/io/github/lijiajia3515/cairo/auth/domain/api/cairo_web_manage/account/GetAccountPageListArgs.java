package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

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

	/**
	 * 注销状态
	 */
	private List<String> logoffStatuses;

	/**
	 * 状态
	 */
	private Boolean enabled;

	/**
	 * 锁定
	 */
	private Boolean locked;

}
