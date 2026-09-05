package io.github.lijiajia3515.cairo.auth.framework.security.cairo_security;

/**
 * 认证类型
 */
public enum CairoSecurityType {
	// 通用身份验证
	/**
	 * 服务身份
	 */
	CLIENT,

	/**
	 * 账号身份
	 */
	ACCOUNT,

	/**
	 * 通用应用级用户
	 */
	APP_USER,

	/**
	 * 通用子应用级用户
	 */
	SUBAPP_USER,

	/**
	 * 通用企业应用级用户
	 */
	TENANT_APP_USER,

	/**
	 * 通用企业子应用级用户
	 */
	TENANT_SUBAPP_USER,

	// 场景化特定身份验证

	/**
	 * 开发平台服务端
	 */
	CAIRO_CLIENT,

	/**
	 * 开发平台用户终端
	 */
	CAIRO_APP_USER,

	/**
	 * 开发平台-运营子应用级用户
	 */
	CAIRO_WEB_MANAGE_USER,
}
