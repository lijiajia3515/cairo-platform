package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.tenant_app_user_login_log;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 企业应用级用户登录日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppUserLoginLog implements Serializable {
	/**
	 * 记录ID
	 */
	private String logId;
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
	private String tenantIcon;
	/**
	 * 应用ID
	 */
	private String appId;
	/**
	 * 应用名称
	 */
	private String appName;
	/**
	 * 应用图标
	 */
	private String appIcon;

	/**
	 * clientId
	 */
	private String clientId;

	/**
	 * 客户端名称
	 */
	private String clientName;

	/**
	 * 登录时间
	 */
	private LocalDateTime loginTime;

	/**
	 * 登录用户
	 */
	private TenantAppUser user;

	/**
	 * 用户TokenId
	 */
	private String tenantAppUserTokenId;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 是否成功
	 */
	private Boolean success;

	/**
	 * 错误原因
	 */
	private String errMsg;

	/**
	 * ip
	 */
	private String ip;

	/**
	 * region
	 */
	private String region;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 登录平台
	 */
	private String platform;

	/**
	 * 引擎
	 */
	private String engine;

	/**
	 * 程序名称
	 */
	private String app;
}
