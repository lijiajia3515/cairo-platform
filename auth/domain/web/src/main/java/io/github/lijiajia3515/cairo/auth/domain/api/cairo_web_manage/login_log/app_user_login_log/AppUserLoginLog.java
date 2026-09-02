package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.app_user_login_log;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 应用用户登录日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserLoginLog implements Serializable {
	/**
	 * 记录ID
	 */
	private String logId;
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
	 * 登录应用用户
	 */
	private AppUser user;

	/**
	 * 用户TokenId
	 */
	private String appUserTokenId;

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
