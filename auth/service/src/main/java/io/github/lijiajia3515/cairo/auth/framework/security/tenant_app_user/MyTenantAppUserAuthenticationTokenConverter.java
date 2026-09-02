package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyTenantAppUserAuthenticationTokenConverter implements TenantAppUserAuthenticationTokenConverter {
	private final CairoAuthTenantAppUserService cairoAuthTenantAppUserService;
	private final TenantAppUserAuthorizationService tenantAppUserAuthorizationService;

	public MyTenantAppUserAuthenticationTokenConverter(CairoAuthTenantAppUserService cairoAuthTenantAppUserService,
															   TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		this.cairoAuthTenantAppUserService = cairoAuthTenantAppUserService;
		this.tenantAppUserAuthorizationService = tenantAppUserAuthorizationService;
	}

	@Override
	public Authentication convert(TenantAppUserAuthenticationTokenRequest tokenRequest) {
		try {
			OAuth2Authorization oAuth2Authorization = tenantAppUserAuthorizationService.findByToken(
				tokenRequest.getTenantId(),
				tokenRequest.getAppId(),
				tokenRequest.getEndpointId(),
				tokenRequest.getToken(),
				CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN
			);
			OAuth2Authorization.Token<OAuthTenantAppUserAccessToken> token = Optional.ofNullable(oAuth2Authorization).map(x -> x.getToken(OAuthTenantAppUserAccessToken.class)).orElse(null);

			if (token == null) {
				throw new TokenInvalidException("token错误");
			}

			if (!token.isActive()) {
				throw new TokenInvalidException("登录失效");
			}

			if (token.isExpired()) {
				throw new TokenExpiredException("登录过期");
			}
			OAuthTenantAppUserAccessToken accessToken = token.getToken();
			List<SimpleGrantedAuthority> tokenAuthorities = accessToken.getScopes().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			Collection<GrantedAuthority> userAuthorities;

			String tenantId = oAuth2Authorization.getAttribute("tenantId");
			String appId = oAuth2Authorization.getAttribute("appId");
			String endpointId = oAuth2Authorization.getAttribute("endpointId");
			LoginType loginType = new LoginType(oAuth2Authorization.getAttribute("loginType"));
			String snsType = oAuth2Authorization.getAttribute("snsType");
			String clientId = oAuth2Authorization.getAttribute("clientId");
			String userId = oAuth2Authorization.getAttribute("userId");

			CairoAuthTenantAppUser user = cairoAuthTenantAppUserService.loadTenantAppUserByUserId(loginType, tenantId, appId, endpointId, clientId, userId);

			CairoOAuthTenantAppUserPrincipal principal = CairoOAuthTenantAppUserPrincipal.builder()
				.id(oAuth2Authorization.getId())
				.loginType(loginType)
				.snsType(snsType)
				.tenantId(tenantId)
				.appId(appId)
				.endpointId(endpointId)
				.clientId(clientId)
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.phoneNumber(user.getPhoneNumber())
				.appAdmin(user.isAppAdmin())
				.roles(user.getRoles())
				.position(user.getPosition())
				.departments(user.getDepartments())
				.tags(user.getTags())
				.userEnabled(user.isUserEnabled())
				.accountId(user.getAccountId())
				.accountAvatarUrl(user.getAccountAvatarUrl())
				.accountNickname(user.getAccountNickname())
				.accountUsername(user.getAccountUsername())
				.accountPhoneNumber(user.getAccountPhoneNumber())
				.accountEmail(user.getAccountEmail())
				.accountLocked(user.isAccountLocked())
				.accountEnabled(user.isAccountEnabled())
				.build();


			if (principal.getAccountLocked()) {
				throw new LockedException("账号已锁定");
			}

			if (!principal.getAccountEnabled()) {
				throw new DisabledException("账号已禁用");
			}

			if (!principal.getUserEnabled()) {
				throw new TenantAppUserDisabledException("用户已禁用");
			}

			userAuthorities = user.getAuthorities();

			List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
			return new CairoOAuthTenantAppUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
