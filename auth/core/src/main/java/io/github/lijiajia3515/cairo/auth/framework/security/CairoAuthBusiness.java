package io.github.lijiajia3515.cairo.auth.framework.security;

import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true, fluent = true)
public enum CairoAuthBusiness implements Business {

	/**
	 * 凭证必须
	 */
	UNAUTHORIZED("Auth.Unauthorized", "必须进行认证"),

	/**
	 * 错误token
	 */
	TOKEN_INVALID("Auth.TokenInvalid", "登录错误"),

	/**
	 * 凭证过期
	 */
	TOKEN_EXPIRED("Auth.TokenExpired", "登录过期"),

	/**
	 * 凭证错误
	 */
	PASSWORD_BAD("Auth.PasswordBad", "密码错误"),

	/**
	 * 验证码错误
	 */
	VERIFY_CODE_BAD("Auth.VerifyCodeBad", "验证码错误"),

	/**
	 * 授权码错误
	 */
	SNS_CODE_BAD("Auth.SnsCodeBad", "授权码错误"),


	/**
	 * 密码过期导致的认证过期
	 */
	NONCE_EXPIRED("Auth.NonceExpired", "Nonce Expired"),

	// ====================== account start ======================

	/**
	 * 账号不存在
	 */
	ACCOUNT_NOT_FOUND("Auth.AccountNotFound", "账号不存在"),

	/**
	 * 账号已锁定
	 */
	ACCOUNT_LOCKED("Auth.AccountLocked", "账号已锁定"),

	/**
	 * 账号已禁用
	 */
	ACCOUNT_DISABLED("Auth.AccountDisabled", "账号已禁用"),

	// ====================== account end ======================


	// ====================== client start ======================
	/**
	 * 客户端不存在
	 */
	CLIENT_NOT_FOUND("Auth.ClientNotFound", "客户端不存在"),

	/**
	 * 客户端被禁用
	 */
	CLIENT_DISABLED("Auth.ClientDisabled", "客户端被禁用"),
	// ====================== client end ======================

	// ====================== app start ======================
	/**
	 * 应用不存在
	 */
	APP_NOT_FOUND("Auth.AppNotFound", "应用不存在"),

	/**
	 * 应用被禁用
	 */
	APP_DISABLED("Auth.AppDisabled", "应用被禁用"),
	// ====================== app start ======================

	// ====================== app endpoint start ======================
	/**
	 * 终端不存在
	 */
	ENDPOINT_NOT_FOUND("Auth.EndpointNotFound", "终端不存在"),

	/**
	 * 终端被禁用
	 */
	ENDPOINT_DISABLED("Auth.EndpointDisabled", "终端被禁用"),
	// ====================== app endpoint end ======================

	/**
	 * 子应用不存在
	 */
	SUBAPP_NOT_FOUND("Auth.SubappNotFound", "子应用不存在"),
	/**
	 * 子应用权限未开通
	 */
	SUBAPP_NOT_APPLY("Auth.SubappNotApply", "子应用未开通"),

	/**
	 * 子应用禁用
	 */
	SUBAPP_DISABLED("Auth.SubappDisabled", "子应用被禁用"),
	// ====================== app subapp end ======================

	// ====================== app user start ======================
	/**
	 * 应用级用户不存在
	 */
	APP_USER_NOT_FOUND("Auth.AppUserNotFound", "用户不存在"),

	/**
	 * 应用级用户被禁用
	 */
	APP_USER_DISABLED("Auth.AppUserDisabled", "用户被禁用"),
	// ====================== app user end ======================

	// ====================== tenant start ======================
	/**
	 * 企业不存在
	 */
	TENANT_NOT_FOUND("Auth.TenantNotFound", "企业不存在"),

	/**
	 * 企业被禁用
	 */
	TENANT_DISABLED("Auth.TenantDisabled", "企业被禁用"),

	// ====================== tenant end ======================

	// ====================== tenant app start ======================

	/**
	 * 企业应用未申请
	 */
	TENANT_APP_NOT_APPLY("Auth.TenantAppNotApply", "企业应用未开通"),

	/**
	 * 企业应用被禁用
	 */
	TENANT_APP_DISABLED("Auth.TenantAppDisabled", "企业应用被禁用"),
	// ====================== tenant app end ======================

	// ====================== tenant app endpoint start ======================
	/**
	 * 企业终端未申请
	 */
	TENANT_ENDPOINT_NOT_APPLY("Auth.TenantEndpointNotApply", "企业终端未开通"),

	/**
	 * 企业终端被禁用
	 */
	TENANT_ENDPOINT_DISABLED("Auth.TenantEndpointDisabled", "企业终端被禁用"),
	// ====================== tenant app endpoint end ======================

	// ====================== tenant app subapp start ======================
	/**
	 * 企业子应用未开通
	 */
	TENANT_SUBAPP_NOT_APPLY("Auth.TenantSubappNotApply", "企业子应用未开通"),
	/**
	 * 企业子应用未开通
	 */
	TENANT_SUBAPP_DISABLED("Auth.TenantSubappDisabled", "企业子应用被禁用"),
	// ====================== tenant app subapp end ======================

	// ====================== tenant app user start ======================
	/**
	 * 用户不存在
	 */
	TENANT_APP_USER_NOT_FOUND("Auth.TenantAppUserNotFound", "用户不存在"),

	/**
	 * 用户被禁用
	 */
	TENANT_APP_USER_DISABLED("Auth.TenantAppUserDisabled", "用户被禁用"),
	// ====================== tenant app user end ======================

	/**
	 * 权限不足
	 */
	DENIED("Auth.Denied", "权限不足"),

	/**
	 * 认证类型不支持
	 */
	NOT_SUPPORTED("Auth.NotSupported", "认证类型不支持"),

	/**
	 * 认证错误
	 */
	ERROR("Auth.Error", "认证错误"),
	;

	private final String code;
	private final String message;

	CairoAuthBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
