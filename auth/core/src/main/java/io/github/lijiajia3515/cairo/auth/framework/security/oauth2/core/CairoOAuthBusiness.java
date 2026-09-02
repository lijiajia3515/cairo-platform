package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true, fluent = true)
public enum CairoOAuthBusiness implements Business {
	/**
	 * 凭证错误
	 */
	OAUTH_ERROR("Auth.OAuthError", "认证错误");


	private final String code;
	private final String message;

	CairoOAuthBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
