package io.github.lijiajia3515.cairo.auth.domain.dto.tenant;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 租户
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Tenant implements Serializable {

	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	private String tenantName;


	/**
	 * 企业名称
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
