package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.CLIENT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;

public class CairoOAuthJwtAuthenticationConverter implements CairoJwtAuthenticationConverter {
	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
	private final CairoAuthAccountService cairoAuthAccountService;
	private final CairoAuthTenantAppUserService cairoAuthTenantAppUserService;
	private final CairoAuthAppUserService cairoAuthAppUserService;
	private final AccountAuthorizationService accountAuthorizationService;
	private final AppUserAuthorizationService appUserAuthorizationService;
	private final TenantAppUserAuthorizationService tenantAppUserAuthorizationService;


	public CairoOAuthJwtAuthenticationConverter(CairoAuthAccountService cairoAuthAccountService,
												CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
												CairoAuthAppUserService cairoAuthAppUserService,
												AccountAuthorizationService accountAuthorizationService,
												AppUserAuthorizationService appUserAuthorizationService,
												TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.cairoAuthTenantAppUserService = cairoAuthTenantAppUserService;
		this.cairoAuthAppUserService = cairoAuthAppUserService;
		this.accountAuthorizationService = accountAuthorizationService;
		this.appUserAuthorizationService = appUserAuthorizationService;
		this.tenantAppUserAuthorizationService = tenantAppUserAuthorizationService;
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
	}

	/**
	 * 会话状态检查：登出/下线/拉黑后即使 JWT 签名有效也拒绝（无状态 token 的吊销闸）
	 */
	private <T extends OAuth2Token> void checkAuthorizationActive(OAuth2Authorization authorization, Class<T> tokenClass) {
		if (authorization == null) {
			throw new TokenInvalidException("登录已失效");
		}
		OAuth2Authorization.Token<T> authorizationToken = authorization.getToken(tokenClass);
		if (authorizationToken == null || !authorizationToken.isActive()) {
			throw new TokenInvalidException("登录已失效");
		}
		if (authorizationToken.isExpired()) {
			throw new TokenExpiredException("登录过期");
		}
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt token) {
		String type = token.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
		if (CLIENT.getValue().equals(type)) {
			return clientAuthenticationToken(token);
		} else if (ACCOUNT.getValue().equals(type)) {
			return accountAuthenticationToken(token);
		} else if (APP_USER.getValue().equals(type)) {
			return appUserAuthenticationToken(token);
		} else if (TENANT_APP_USER.getValue().equals(type)) {
			return tenantAppUserAuthenticationToken(token);
		} else {
			throw new ProviderNotFoundException("不支持的认证类型: " + type);
		}
	}


	@NewSpan
	CairoOAuthClientAuthenticationToken clientAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String clientTokenId = jwt.getSubject();

		OAuth2AccessToken token = new OAuth2AccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			jwt.getTokenValue(),
			jwt.getIssuedAt(),
			jwt.getExpiresAt(),
			scopes
		);

		CairoOAuthClientPrincipal principal = CairoOAuthClientPrincipal.builder()
			.id(clientTokenId)
			.loginType(loginTypeValue)
			.appId(appId)
			.clientId(clientId)
			.build();

		Collection<GrantedAuthority> jwtAuthorities = grantedAuthoritiesConverter.convert(jwt);
		return new CairoOAuthClientAuthenticationToken(token, principal, jwtAuthorities);
	}

	@NewSpan
	CairoOAuthAccountAuthenticationToken accountAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String accountId = jwt.getClaimAsString(CairoOAuthParameterNames.ACCOUNT_ID);
		String accountTokenId = jwt.getSubject();

		OAuthAccountAccessToken token = new OAuthAccountAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			accountId,
			accountTokenId,
			jwt.getTokenValue(),
			jwt.getIssuedAt(),
			jwt.getExpiresAt(),
			scopes,
			jwt.getClaims()
		);
		Collection<GrantedAuthority> jwtAuthorities = grantedAuthoritiesConverter.convert(jwt);

		CairoOAuthAccountPrincipal principal;
		Collection<GrantedAuthority> accountAuthorities;
		try {
			checkAuthorizationActive(accountAuthorizationService.findByToken(jwt.getTokenValue(), CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN), OAuthAccountAccessToken.class);
			CairoAuthAccount account = cairoAuthAccountService.loadAccountByAccountId(loginType, accountId);
			principal = CairoOAuthAccountPrincipal.builder()
				.id(accountTokenId)
				.loginType(loginType)
				.appId(appId)
				.clientId(clientId)
				.accountId(accountId)
				.nickname(account.getNickname())
				.username(account.getLoginname())
				.phoneNumber(account.getPhoneNumber())
				.email(account.getEmail())
				.avatarUrl(account.getAvatarUrl())
				.roles(Collections.emptyList())
				.departments(Collections.emptyList())
				.tags(Collections.emptyList())
				.locked(account.isLocked())
				.enabled(account.isEnabled())
				.build();

			if (principal.getLocked()) {
				throw new LockedException("账号已锁定");
			}
			if (!principal.getEnabled()) {
				throw new DisabledException("账号已禁用");
			}

			accountAuthorities = account.getAuthorities();
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(jwtAuthorities, accountAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthAccountAuthenticationToken(token, principal, authorities);
	}

	@NewSpan
	CairoOAuthTenantAppUserAuthenticationToken tenantAppUserAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String endpointId = jwt.getClaimAsString(CairoOAuthParameterNames.ENDPOINT_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String tenantId = jwt.getClaimAsString(CairoOAuthParameterNames.TENANT_ID);
		String userId = jwt.getClaimAsString(CairoOAuthParameterNames.USER_ID);
		String subappId = jwt.getClaimAsString(CairoOAuthParameterNames.SUBAPP_ID);
		String subappVersion = jwt.getClaimAsString(CairoOAuthParameterNames.SUBAPP_VERSION);
		String tenantAppUserTokenId = jwt.getSubject();

		OAuthTenantAppUserAccessToken token = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			tenantId,
			appId,
			endpointId,
			userId,
			tenantAppUserTokenId,
			jwt.getTokenValue(),
			jwt.getIssuedAt(),
			jwt.getExpiresAt(),
			scopes,
			jwt.getClaims()
		);
		Collection<GrantedAuthority> tokenAuthorities = grantedAuthoritiesConverter.convert(jwt);

		CairoOAuthTenantAppUserPrincipal principal;
		Collection<GrantedAuthority> tenantAppUserAuthorities;
		try {
			checkAuthorizationActive(tenantAppUserAuthorizationService.findByToken(tenantId, appId, endpointId, jwt.getTokenValue(), CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN), OAuthTenantAppUserAccessToken.class);
			CairoAuthTenantAppUser authTenantAppUser = cairoAuthTenantAppUserService.loadTenantAppUserByUserId(loginType, tenantId, appId, endpointId, clientId, userId);
			principal = CairoOAuthTenantAppUserPrincipal.builder()
				.id(tenantAppUserTokenId)
				.loginType(loginType)
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.position(authTenantAppUser.getPosition())
				.userId(authTenantAppUser.getUserId())
				.nickname(authTenantAppUser.getNickname())
				.phoneNumber(authTenantAppUser.getPhoneNumber())
				.userEnabled(authTenantAppUser.isUserEnabled())
				.appAdmin(authTenantAppUser.isAppAdmin())
				.roles(authTenantAppUser.getRoles())
				.departments(authTenantAppUser.getDepartments())
				.tags(authTenantAppUser.getTags())
				.accountId(authTenantAppUser.getAccountId())
				.accountNickname(authTenantAppUser.getAccountNickname())
				.accountUsername(authTenantAppUser.getAccountUsername())
				.accountPhoneNumber(authTenantAppUser.getAccountPhoneNumber())
				.accountAvatarUrl(authTenantAppUser.getAccountAvatarUrl())
				.accountLocked(authTenantAppUser.isAccountLocked())
				.accountEnabled(authTenantAppUser.isAccountEnabled())
				.build();

			if (authTenantAppUser.isAccountLocked()) {
				throw new LockedException("账号已锁定");
			}
			if (!authTenantAppUser.isAccountEnabled()) {
				throw new DisabledException("账号已禁用");
			}
			if (!authTenantAppUser.isUserEnabled()) {
				throw new DisabledException("用户已禁用");
			}

			tenantAppUserAuthorities = authTenantAppUser.getAuthorities();
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, tenantAppUserAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthTenantAppUserAuthenticationToken(token, principal, authorities);
	}

	@NewSpan
	CairoOAuthAppUserAuthenticationToken appUserAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String endpointId = jwt.getClaimAsString(CairoOAuthParameterNames.ENDPOINT_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String userId = jwt.getClaimAsString(CairoOAuthParameterNames.USER_ID);
		String tokenId = jwt.getSubject();

		OAuthAppUserAccessToken token = new OAuthAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			appId,
			endpointId,
			userId,
			tokenId,
			jwt.getTokenValue(),
			jwt.getIssuedAt(),
			jwt.getExpiresAt(),
			scopes,
			jwt.getClaims()
		);
		Collection<GrantedAuthority> tokenAuthorities = grantedAuthoritiesConverter.convert(jwt);

		CairoOAuthAppUserPrincipal principal;
		Collection<GrantedAuthority> appUserAuthorities;
		try {
			checkAuthorizationActive(appUserAuthorizationService.findByToken(appId, endpointId, jwt.getTokenValue(), CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN), OAuthAppUserAccessToken.class);
			CairoAuthAppUser authAppUser = cairoAuthAppUserService.loadAppUserByAppUserId(loginType, appId, endpointId, clientId, userId);
			principal = CairoOAuthAppUserPrincipal.builder()
				.id(tokenId)
				.loginType(loginType)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.position(authAppUser.getPosition())
				.userId(authAppUser.getUserId())
				.nickname(authAppUser.getNickname())
				.phoneNumber(authAppUser.getPhoneNumber())
				.userEnabled(authAppUser.isUserEnabled())
				.appAdmin(authAppUser.isAppAdmin())
				.roles(authAppUser.getRoles())
				.departments(authAppUser.getDepartments())
				.tags(authAppUser.getTags())
				.accountId(authAppUser.getAccountId())
				.accountNickname(authAppUser.getAccountNickname())
				.accountUsername(authAppUser.getAccountUsername())
				.accountPhoneNumber(authAppUser.getAccountPhoneNumber())
				.accountAvatarUrl(authAppUser.getAccountAvatarUrl())
				.accountLocked(authAppUser.isAccountLocked())
				.accountEnabled(authAppUser.isAccountEnabled())
				.build();

			if (authAppUser.isAccountLocked()) {
				throw new LockedException("账号已锁定");
			}
			if (!authAppUser.isAccountEnabled()) {
				throw new DisabledException("账号已禁用");
			}
			if (!authAppUser.isUserEnabled()) {
				throw new DisabledException("用户已禁用");
			}

			appUserAuthorities = authAppUser.getAuthorities();
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, appUserAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthAppUserAuthenticationToken(token, principal, authorities);
	}
}
