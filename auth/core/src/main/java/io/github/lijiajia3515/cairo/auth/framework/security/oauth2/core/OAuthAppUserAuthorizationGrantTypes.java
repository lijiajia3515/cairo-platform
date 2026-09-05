package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 应用级应用级用户授权
 */
public interface OAuthAppUserAuthorizationGrantTypes {
	AuthorizationGrantType ACCOUNT_PASSWORD = new AuthorizationGrantType("app_user:password");

	AuthorizationGrantType ACCOUNT_VERIFY_CODE = new AuthorizationGrantType("app_user:verify_code");
	AuthorizationGrantType ACCOUNT_SNS_CODE = new AuthorizationGrantType("app_user:account_sns_code");

	AuthorizationGrantType ACCOUNT_ACCESS_TOKEN = new AuthorizationGrantType("app_user:account_access_token");

	AuthorizationGrantType APP_USER_REFRESH_TOKEN = new AuthorizationGrantType("app_user:app_user_refresh_token");


}
