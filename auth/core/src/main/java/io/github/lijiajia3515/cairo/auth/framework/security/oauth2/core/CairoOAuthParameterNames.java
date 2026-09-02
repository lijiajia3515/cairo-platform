package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

public interface CairoOAuthParameterNames {
	/**
	 * 认证类型
	 */
	String ACCESS_TOKEN_FORMAT = "access_token_format";

	/**
	 * 账号访问令牌
	 */
	String ACCOUNT_ACCESS_TOKEN = "account_access_token";

	/**
	 * 账号刷新令牌
	 */
	String ACCOUNT_REFRESH_TOKEN = "account_refresh_token";

	/**
	 * 用户访问令牌
	 */
	String TENANT_APP_USER_ACCESS_TOKEN = "tenant_app_user_access_token";


	/**
	 * 认证类型
	 */
	String AUTH_TYPE = "auth_type";

	/**
	 * 登录类型
	 */
	String LOGIN_TYPE = "login_type";

	/**
	 * 验证码授权模式-手机号
	 */
	String ACCOUNT_PHONE_NUMBER = "account_phone_number";

	/**
	 * 验证码授权模式-手机号
	 */
	String PHONE_NUMBER = "phone_number";

	/**
	 * 验证码授权模式-验证码
	 */
	String VERIFY_CODE = "verify_code";

	/**
	 * appid
	 */
	String APP_ID = "app_id";

	/**
	 * 终端ID
	 */
	String ENDPOINT_ID = "endpoint_id";

	/**
	 * 租户id
	 */
	String TENANT_ID = "tenant_id";

	/**
	 * 第三方认证类型
	 */
	String SNS_TYPE = "sns_type";

	/**
	 * 第三方认证-提供商ID
	 */
	String SNS_PROVIDER_ID = "sns_provider_id";

	/**
	 * 第三方认证授权码
	 */
	String SNS_CODE = "sns_code";
	/**
	 * 账号id
	 */
	String ACCOUNT_ID = "account_id";

	/**
	 * 用户id
	 */
	String USER_ID = "user_id";

	/**
	 * 用户名
	 */
	String USERNAME = "username";

	/**
	 * 邮箱
	 */
	String EMAIL = "email";

	/**
	 * 名称/昵称
	 */
	String NICKNAME = "nickname";

	/**
	 * 头像
	 */
	String AVATAR_URL = "avatar_url";

	/**
	 * 角色
	 */
	String ROLES = "roles";

	/**
	 * 部门
	 */
	String DEPARTMENTS = "departments";

	/**
	 * 标签
	 */
	String TAGS = "tags";

	String APP_ADMIN = "app_admin";

	/**
	 * 权限
	 */
	String AUTHORITIES = "authorities";

	/**
	 * 子应用ID
	 */
	String SUBAPP_ID = "subapp_id";

	/**
	 * 子应用版本
	 */
	String SUBAPP_VERSION = "subapp_version";

}
