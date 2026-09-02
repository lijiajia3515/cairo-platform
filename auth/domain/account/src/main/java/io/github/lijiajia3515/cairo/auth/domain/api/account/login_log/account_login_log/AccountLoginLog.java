package io.github.lijiajia3515.cairo.auth.domain.api.account.login_log.account_login_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 账号登录日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLoginLog implements Serializable {
	/**
	 * 记录ID
	 */
	private String logId;

	/**
	 * 认证类型
	 */
	private String authType;

	/**
	 * 第三方认证类型
	 */
	private String snsType;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * clientId
	 */
	private String clientId;

	/**
	 * 登录方式
	 */
	private String loginType;

	/**
	 * 登录时间
	 */
	private LocalDateTime loginTime;

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
	 * 登录方式
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
