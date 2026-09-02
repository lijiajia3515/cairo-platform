package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 账号级授权
 */
public interface OAuthAccountAuthorizationGrantTypes {

	AuthorizationGrantType ACCOUNT_PASSWORD = new AuthorizationGrantType("account:password");

	AuthorizationGrantType ACCOUNT_VERIFY_CODE = new AuthorizationGrantType("account:verify_code");


	AuthorizationGrantType ACCOUNT_SNS_CODE = new AuthorizationGrantType("account:sns_code");

	AuthorizationGrantType ACCOUNT_REFRESH_TOKEN = new AuthorizationGrantType("account:account_refresh_token");
}
