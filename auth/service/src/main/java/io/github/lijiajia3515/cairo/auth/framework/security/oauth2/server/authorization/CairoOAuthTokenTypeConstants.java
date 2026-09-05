package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;

import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

public interface CairoOAuthTokenTypeConstants {

	// IdToken
    OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);


	// 账号级用户token
    OAuth2TokenType ACCOUNT_ACCESS_TOKEN = new OAuth2TokenType("account:access_token");
    OAuth2TokenType ACCOUNT_REFRESH_TOKEN = new OAuth2TokenType("account:refresh_token");


	// 应用级应用级用户token
	OAuth2TokenType APP_USER_ACCESS_TOKEN = new OAuth2TokenType("app_user:access_token");

	OAuth2TokenType APP_USER_REFRESH_TOKEN = new OAuth2TokenType("app_user:refresh_token");


	// 企业级应用级用户token
	OAuth2TokenType TENANT_APP_USER_ACCESS_TOKEN = new OAuth2TokenType("tenant_app_user:access_token");
	OAuth2TokenType TENANT_APP_USER_REFRESH_TOKEN = new OAuth2TokenType("tenant_app_user:refresh_token");

}
