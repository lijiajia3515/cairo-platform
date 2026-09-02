package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.CLIENT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;

public class CairoJwtContextCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
	@Override
	public void customize(JwtEncodingContext context) {

		OAuth2TokenType tokenType = context.getTokenType();
		if (CairoOAuthTokenTypeConstants.ID_TOKEN_TOKEN_TYPE.equals(tokenType)) {
			// id_token

		} else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
			// access_token
			Authentication authentication = context.getPrincipal();
			context.getClaims().subject(context.getAuthorization().getId());

			CairoRegisteredClient registeredClient = (CairoRegisteredClient) context.getRegisteredClient();

			context.getClaims().claim(CairoOAuthParameterNames.APP_ID, registeredClient.getAppId()); // 应用标识
			context.getClaims().claim(OAuth2ParameterNames.CLIENT_ID, registeredClient.getClientId()); // client标识

			if (authentication instanceof OAuth2ClientAuthenticationToken) {
				OAuth2ClientAuthenticationToken clientToken = (OAuth2ClientAuthenticationToken) authentication;
				customCairoClient(context, clientToken);
			} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
				Object principal = authentication.getPrincipal();
				if (principal instanceof CairoAuthAccount) {
					CairoAuthAccount accountToken = (CairoAuthAccount) principal;
					customCairoAccount(context, accountToken);
				} else if (principal instanceof CairoAuthTenantAppUser) {
					CairoAuthTenantAppUser userToken = (CairoAuthTenantAppUser) principal;
					customCairoEndpointUser(context, userToken);
				}
			}
		}
	}

	void customCairoClient(JwtEncodingContext context, OAuth2ClientAuthenticationToken token) {
		context.getClaims().claim(CairoOAuthParameterNames.AUTH_TYPE, CLIENT.getValue());
		context.getClaims().claim(CairoOAuthParameterNames.LOGIN_TYPE, token.getClientAuthenticationMethod().getValue());
	}

	void customCairoAccount(JwtEncodingContext context, CairoAuthAccount account) {
		context.getClaims().claim(CairoOAuthParameterNames.AUTH_TYPE, ACCOUNT.getValue());
		context.getClaims().claim(CairoOAuthParameterNames.LOGIN_TYPE, account.getLoginType().getValue());
		context.getClaims().claim(CairoOAuthParameterNames.ACCOUNT_ID, account.getAccountId());
		// Optional.ofNullable(account.getLoginName()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.USERNAME, x));
		// Optional.ofNullable(account.getName()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.NAME, x));
		// Optional.ofNullable(account.getAvatarUrl()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.AVATAR_URL, x));
		// Optional.ofNullable(account.getPhoneNumber()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.PHONE_NUMBER, x));
		// Optional.ofNullable(account.getEmail()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.EMAIL, x));
		// context.getClaims().claim(OAuthParameterNames.AUTHORITIES, account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
	}



	void customCairoEndpointUser(JwtEncodingContext context, CairoAuthTenantAppUser endpointUser) {
		context.getClaims().claim(CairoOAuthParameterNames.AUTH_TYPE, TENANT_APP_USER.getValue());
		context.getClaims().claim(CairoOAuthParameterNames.LOGIN_TYPE, endpointUser.getLoginType().getValue());
		context.getClaims().claim(CairoOAuthParameterNames.TENANT_ID, endpointUser.getTenantId());
		context.getClaims().claim(CairoOAuthParameterNames.ENDPOINT_ID, endpointUser.getEndpointId());
		context.getClaims().claim(CairoOAuthParameterNames.USER_ID, endpointUser.getUserId());
		// context.getClaims().claim(OAuthParameterNames.ACCOUNT_ID, endpointUser.getAccountId());
		// Optional.ofNullable(endpointUser.getLoginName()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.USERNAME, x));
		// Optional.ofNullable(endpointUser.getName()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.NAME, x));
		// Optional.ofNullable(endpointUser.getAvatarUrl()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.AVATAR_URL, x));
		// Optional.ofNullable(endpointUser.getPhoneNumber()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.PHONE_NUMBER, x));
		// Optional.ofNullable(endpointUser.getEmail()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.EMAIL, x));

		// Optional.ofNullable(endpointUser.getRoles()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.ROLES, x.stream().map(OAuthJwtConverter::cairoRole2String).collect(Collectors.toList())));
		// Optional.ofNullable(endpointUser.getDepartments()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.DEPARTMENTS, x.stream().map(OAuthJwtConverter::cairoDepartment2String).collect(Collectors.toList())));
		// Optional.ofNullable(endpointUser.getTags()).ifPresent(x -> context.getClaims().claim(OAuthParameterNames.TAGS, x.stream().map(OAuthJwtConverter::cairoTag2String).collect(Collectors.toList())));

		// context.getClaims().claim(OAuthParameterNames.APP_ADMIN, endpointUser.getAppAdmin());

		// context.getClaims().claim("authorities", endpointUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" ")));
		// context.getClaims().claim(OAuthParameterNames.AUTHORITIES, endpointUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
	}
}


