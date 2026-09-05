package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.client_login_log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 应用级用户登录日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLoginLog implements Serializable {
	/**
	 * 记录ID
	 */
	private String logId;

	/**
	 * 登录时间
	 */
	private LocalDateTime loginTime;

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
	 * 客户端TokenId
	 */
	private String clientTokenId;

	/**
	 * 授权类型
	 */
	private String grantType;

	/**
	 * 认证方式
	 */
	private String method;

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
