package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 企业级终端用户授权
 */
public interface OAuthTenantAppUserAuthorizationTypes {
	AuthorizationGrantType ACCOUNT_PASSWORD = new AuthorizationGrantType("tenant_app_user:password");

	AuthorizationGrantType ACCOUNT_VERIFY_CODE = new AuthorizationGrantType("tenant_app_user:verify_code");

	AuthorizationGrantType ACCOUNT_SNS_CODE = new AuthorizationGrantType("tenant_app_user:account_sns_code");

	AuthorizationGrantType ACCOUNT_ACCESS_TOKEN = new AuthorizationGrantType("tenant_app_user:account_access_token");

	AuthorizationGrantType TENANT_APP_USER_REFRESH_TOKEN = new AuthorizationGrantType("tenant_app_user:tenant_app_user_refresh_token");


}
