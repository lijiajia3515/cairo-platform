package io.github.lijiajia3515.cairo.auth.domain.api.open.tenant;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开的企业信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenTenant {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	private String tenantName;


	/**
	 * 别名
	 */
	private String aliasName;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 拥有者账号
	 */
	private Account ownerAccount;

	/**
	 * 是否开启
	 */
	private Boolean enabled;
}
