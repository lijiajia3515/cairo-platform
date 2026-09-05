package io.github.lijiajia3515.cairo.auth.domain.api.account.tenant_app_user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * tenant user
 */
@Data
@Builder
public class TenantAppUser {
	/**
	 * 企业标识
	 */
	private String tenantId;

	/**
	 * 名称
	 */
	private String tenantName;

	/**
	 * 企业图标
	 */
	private String tenantIcon;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户名称
	 */
	private String nickname;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinTime;

}
