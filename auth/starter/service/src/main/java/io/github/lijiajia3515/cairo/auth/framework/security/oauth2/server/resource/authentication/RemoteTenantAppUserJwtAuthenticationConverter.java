package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserPrincipalModel;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
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
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;

@Slf4j
public class RemoteTenantAppUserJwtAuthenticationConverter implements CairoJwtAuthenticationConverter {
	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
	private final TenantAppUserClientApiService tenantAppUserClientApiService;

	public RemoteTenantAppUserJwtAuthenticationConverter(TenantAppUserClientApiService tenantAppUserClientApiService) {
		this.tenantAppUserClientApiService = tenantAppUserClientApiService;
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String type = jwt.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
		if (!TENANT_APP_USER.getValue().equals(type)) {
			return null;
		}

		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());

		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);

		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
		String endpointId = jwt.getClaimAsString(CairoOAuthParameterNames.ENDPOINT_ID);
		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
		String tenantId = jwt.getClaimAsString(CairoOAuthParameterNames.TENANT_ID);
		String userId = jwt.getClaimAsString(CairoOAuthParameterNames.USER_ID);
		String endpointUserTokenId = jwt.getSubject();

		OAuthTenantAppUserAccessToken token = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			tenantId,
			appId,
			endpointId,
			userId,
			endpointUserTokenId,
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
			TenantAppUserAuthModel tenantAppUserAuthModel = Optional.ofNullable(tenantAppUserAuth).orElseThrow();

			String status = tenantAppUserAuthModel.getStatus();
			if (!DefaultBusiness.SUCCESS.getCode().equals(status)) {
				// 应用异常
				if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException();
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException();
					//客户端异常
				} else if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException();
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException();

				} else if (CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode().equals(status)) {
					throw new EndpointNotFoundException();
				} else if (CairoAuthBusiness.ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new EndpointDisabledException();
				}
				// 租户异常
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
				} else {
					throw new ProviderNotFoundException("不支持的认证类型");
				}
			}

			TenantAppUserPrincipalModel tenantAppUserPrincipalModel = tenantAppUserAuthModel.getPrincipal();
			principal = CairoOAuthTenantAppUserPrincipal.builder()
				.id(endpointUserTokenId)
				.loginType(loginType)
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(userId)
				.nickname(tenantAppUserPrincipalModel.getNickname())
				.phoneNumber(tenantAppUserPrincipalModel.getPhoneNumber())
				.userEnabled(tenantAppUserPrincipalModel.getUserEnabled())
				.appAdmin(tenantAppUserPrincipalModel.getAppAdmin())
				.roles(tenantAppUserPrincipalModel.getRoles())
				.departments(tenantAppUserPrincipalModel.getDepartments())
				.tags(tenantAppUserPrincipalModel.getTags())
				.position(tenantAppUserPrincipalModel.getPosition())
				.accountId(tenantAppUserPrincipalModel.getAccountId())
				.accountAvatarUrl(tenantAppUserPrincipalModel.getAccountAvatarUrl())
				.accountNickname(tenantAppUserPrincipalModel.getAccountNickname())
				.accountUsername(tenantAppUserPrincipalModel.getAccountUsername())
				.accountPhoneNumber(tenantAppUserPrincipalModel.getAccountPhoneNumber())
				.accountEmail(tenantAppUserPrincipalModel.getAccountEmail())
				.accountLocked(tenantAppUserPrincipalModel.getAccountLocked())
				.accountEnabled(tenantAppUserPrincipalModel.getAccountEnabled())

				.build();

			userAuthorities = tenantAppUserAuthModel.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}

		List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthTenantAppUserAuthenticationToken(token, principal, authorities);

	}
}
