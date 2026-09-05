package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CairoAccountAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserPrincipalModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserPrincipalModel;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
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
import io.github.lijiajia3515.cairo.auth.modules.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client.TenantAppUserClientApiService;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
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

@Slf4j
public class CairoRemoteAuthenticationConverter implements CairoJwtAuthenticationConverter {
	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
	private final AccountClientApiService accountClientApiService;
	private final TenantAppUserClientApiService tenantAppUserClientApiService;
	private final AppUserClientApiService appUserClientApiService;

	public CairoRemoteAuthenticationConverter(AccountClientApiService accountClientApiService,
											  TenantAppUserClientApiService tenantAppUserClientApiService,
											  AppUserClientApiService appUserClientApiService) {
		this.accountClientApiService = accountClientApiService;
		this.tenantAppUserClientApiService = tenantAppUserClientApiService;
		this.appUserClientApiService = appUserClientApiService;
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
		grantedAuthoritiesConverter.setAuthorityPrefix("");
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
			throw new ProviderNotFoundException("不支持的认证类型");
		}
	}

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


	CairoOAuthAccountAuthenticationToken accountAuthenticationToken(Jwt jwt) {

		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String snsType = jwt.getClaimAsString(CairoOAuthParameterNames.SNS_TYPE);

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
		Collection<GrantedAuthority> tokenAuthorities = grantedAuthoritiesConverter.convert(jwt);


		CairoOAuthAccountPrincipal principal;
		Set<SimpleGrantedAuthority> accountAuthorities;
		try {
			CairoAccountAuthModel accountAuth = accountClientApiService.getAccountAuth(GetAccountAuthArgs.builder().appId(appId).clientId(clientId).accountId(accountId).build());
			CairoAccountAuthModel model = Optional.ofNullable(accountAuth).orElseThrow();

			String status = model.getStatus();
			if (!DefaultBusiness.SUCCESS.getCode().equals(status)) {
				// 应用异常
				if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException();
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException();
				}
				// 客户端异常
				if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException();
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException();
				}

				// 账号异常
				else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException();
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException("账号被锁定");
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException("账号被禁用");
				} else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			CairoOAuthAccountPrincipal account = model.getPrincipal();
			principal = CairoOAuthAccountPrincipal.builder()
				.id(accountTokenId)
				.loginType(loginType)
				.snsType(snsType)
				.appId(appId)
				.clientId(clientId)
				.accountId(accountId)
				.avatarUrl(account.getAvatarUrl())
				.nickname(account.getNickname())
				.username(account.getUsername())
				.phoneNumber(account.getPhoneNumber())
				.email(account.getEmail())
				.roles(Collections.emptyList())
				.departments(Collections.emptyList())
				.tags(Collections.emptyList())
				.locked(account.getLocked())
				.enabled(account.getEnabled())
				.build();

			accountAuthorities = model.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, accountAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthAccountAuthenticationToken(token, principal, authorities);
	}

	CairoOAuthTenantAppUserAuthenticationToken tenantAppUserAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String snsType = jwt.getClaimAsString(CairoOAuthParameterNames.SNS_TYPE);
		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String endpointId = jwt.getClaimAsString(CairoOAuthParameterNames.ENDPOINT_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String tenantId = jwt.getClaimAsString(CairoOAuthParameterNames.TENANT_ID);
		String userId = jwt.getClaimAsString(CairoOAuthParameterNames.USER_ID);
		String tokenId = jwt.getSubject();

		OAuthTenantAppUserAccessToken token = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			tenantId,
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

		CairoOAuthTenantAppUserPrincipal principal;
		Collection<GrantedAuthority> userAuthorities;
		try {
			TenantAppUserAuthModel tenantAppUserAuth = tenantAppUserClientApiService.getTenantAppUserAuth(
				GetTenantAppUserAuthArgs.builder()
					.appId(appId)
					.endpointId(endpointId)
					.clientId(clientId)
					.tenantId(tenantId)
					.userId(userId)
					.build()
			);
			TenantAppUserAuthModel model = Optional.ofNullable(tenantAppUserAuth).orElseThrow();

			String status = model.getStatus();
			if (!DefaultBusiness.SUCCESS.getCode().equals(status)) {
				// 应用异常
				if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException();
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException();
					// 客户端异常
				} else if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException();
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException();

				} else if (CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode().equals(status)) {
					throw new EndpointNotFoundException();
				} else if (CairoAuthBusiness.ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new EndpointDisabledException();
				}
				// 企业异常
				else if (CairoAuthBusiness.TENANT_NOT_FOUND.getCode().equals(status)) {
					throw new TenantNotFoundException();
				} else if (CairoAuthBusiness.TENANT_DISABLED.getCode().equals(status)) {
					throw new TenantDisabledException();
				} else if (CairoAuthBusiness.TENANT_APP_NOT_APPLY.getCode().equals(status)) {
					throw new TenantAppNotApplyException();
				} else if (CairoAuthBusiness.TENANT_APP_DISABLED.getCode().equals(status)) {
					throw new TenantAppDisabledException();
				} else if (CairoAuthBusiness.TENANT_ENDPOINT_NOT_APPLY.getCode().equals(status)) {
					throw new TenantEndpointNotApplyException();
				} else if (CairoAuthBusiness.TENANT_ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new TenantEndpointDisabledException();
				}

				// 用户异常
				else if (CairoAuthBusiness.TENANT_APP_USER_NOT_FOUND.getCode().equals(status)) {
					throw new TenantAppUserNotFoundException();
				} else if (CairoAuthBusiness.TENANT_APP_USER_DISABLED.getCode().equals(status)) {
					throw new TenantAppUserDisabledException("用户被禁用");
				}
				// 账号异常
				else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException();
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException("账号被锁定");
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException("账号被禁用");
				}
				// 其他
				else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			TenantAppUserPrincipalModel userModel = model.getPrincipal();
			principal = CairoOAuthTenantAppUserPrincipal.builder()
				.id(tokenId)
				.loginType(loginType)
				.snsType(snsType)
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.nickname(userModel.getNickname())
				.phoneNumber(userModel.getPhoneNumber())
				.appAdmin(userModel.getAppAdmin())
				.roles(userModel.getRoles())
				.departments(userModel.getDepartments())
				.tags(userModel.getTags())
				.position(userModel.getPosition())

				.accountId(userModel.getAccountId())
				.accountAvatarUrl(userModel.getAccountAvatarUrl())
				.accountNickname(userModel.getAccountNickname())
				.accountUsername(userModel.getAccountUsername())
				.accountPhoneNumber(userModel.getAccountPhoneNumber())
				.accountEmail(userModel.getAccountEmail())
				.accountLocked(userModel.getAccountLocked())
				.accountEnabled(userModel.getAccountEnabled())
				.build();
			userAuthorities = Optional.ofNullable(model.getAuthorities()).orElse(Collections.emptyList()).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthTenantAppUserAuthenticationToken(token, principal, authorities);

	}

	CairoOAuthAppUserAuthenticationToken appUserAuthenticationToken(Jwt jwt) {
		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
		String snsType = jwt.getClaimAsString(CairoOAuthParameterNames.SNS_TYPE);
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
		Collection<GrantedAuthority> userAuthorities;
		try {
			AppUserAuthModel appUserAuth = appUserClientApiService.getAppUserAuth(
				GetAppUserAuthArgs.builder()
					.appId(appId)
					.endpointId(endpointId)
					.clientId(clientId)
					.userId(userId)
					.build()
			);
			AppUserAuthModel model = Optional.ofNullable(appUserAuth).orElseThrow();

			String status = model.getStatus();
			if (!DefaultBusiness.SUCCESS.getCode().equals(status)) {
				// 应用异常
				if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException();
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException();
					// 客户端异常
				} else if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException();
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException();

				} else if (CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode().equals(status)) {
					throw new EndpointNotFoundException();
				} else if (CairoAuthBusiness.ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new EndpointDisabledException();
				}
				// 应用级用户异常
				else if (CairoAuthBusiness.APP_USER_NOT_FOUND.getCode().equals(status)) {
					throw new AppUserNotFoundException();
				} else if (CairoAuthBusiness.APP_USER_DISABLED.getCode().equals(status)) {
					throw new AppUserDisabledException();
				}
				// 账号异常
				else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException();
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException("账号被锁定");
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException("账号被禁用");
				}
				// 其他
				else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			AppUserPrincipalModel userModel = model.getPrincipal();
			principal = CairoOAuthAppUserPrincipal.builder()
				.id(tokenId)
				.loginType(loginType)
				.snsType(snsType)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.nickname(userModel.getNickname())
				.phoneNumber(userModel.getPhoneNumber())
				.appAdmin(userModel.getAppAdmin())
				.roles(userModel.getRoles())
				.departments(userModel.getDepartments())
				.tags(userModel.getTags())
				.position(userModel.getPosition())

				.accountId(userModel.getAccountId())
				.accountAvatarUrl(userModel.getAccountAvatarUrl())
				.accountNickname(userModel.getAccountNickname())
				.accountUsername(userModel.getAccountUsername())
				.accountPhoneNumber(userModel.getAccountPhoneNumber())
				.accountEmail(userModel.getAccountEmail())
				.accountLocked(userModel.getAccountLocked())
				.accountEnabled(userModel.getAccountEnabled())
				.build();
			userAuthorities = Optional.ofNullable(model.getAuthorities()).orElse(Collections.emptyList()).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthAppUserAuthenticationToken(token, principal, authorities);

	}

}
