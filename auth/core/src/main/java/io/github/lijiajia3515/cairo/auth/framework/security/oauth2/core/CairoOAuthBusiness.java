package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true, fluent = true)
public enum CairoOAuthBusiness implements Business {
	/**
	 * OAuth2 协议层错误兜底（server_error / temporarily_unavailable / 未识别错误码）
	 */
	OAUTH_ERROR("Auth.OAuthError", "认证错误"),

	/**
	 * invalid_client：客户端无效或密钥错误
	 */
	CLIENT_INVALID("Auth.ClientInvalid", "客户端无效或密钥错误"),

	/**
	 * unauthorized_client / unsupported_grant_type：客户端未开通该 grant_type 或类型不支持
	 */
	GRANT_NOT_SUPPORTED("Auth.GrantNotSupported", "grant_type不支持或未对该客户端开通"),

	/**
	 * invalid_grant：授权凭证无效或已过期（刷新令牌 / 授权码 / 账号凭证置换）
	 */
	GRANT_INVALID("Auth.GrantInvalid", "授权凭证无效或已过期"),

	/**
	 * invalid_request：OAuth 请求参数缺失或非法
	 */
	PARAMS_BAD("Auth.ParamsBad", "OAuth请求参数缺失或非法"),

	/**
	 * invalid_scope / insufficient_scope：scope 超出客户端许可范围
	 */
	SCOPE_INSUFFICIENT("Auth.ScopeInsufficient", "scope权限不足");


	private final String code;
	private final String message;

	CairoOAuthBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
