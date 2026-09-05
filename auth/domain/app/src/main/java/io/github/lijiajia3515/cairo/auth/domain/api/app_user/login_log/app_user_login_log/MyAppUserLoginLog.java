package io.github.lijiajia3515.cairo.auth.domain.api.app_user.login_log.app_user_login_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 用户登录日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyAppUserLoginLog implements Serializable {
	/**
	 * 记录ID
	 */
	private String logId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 终端名称
	 */
	private String endpointName;

	/**
	 * 终端图标
	 */
	private String endpointIcon;

	/**
	 * 客户端ID
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

	/**
	 * region
	 */
	private String region;
}
