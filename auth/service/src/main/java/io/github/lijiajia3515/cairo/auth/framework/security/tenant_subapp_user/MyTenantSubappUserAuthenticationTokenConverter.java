package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
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

public class MyTenantSubappUserAuthenticationTokenConverter implements TenantSubappUserAuthenticationTokenConverter {
	private final CairoAuthTenantSubappUserService cairoAuthTenantSubappUserService;
	private final TenantAppUserAuthorizationService tenantAppUserAuthorizationService;

	public MyTenantSubappUserAuthenticationTokenConverter(CairoAuthTenantSubappUserService cairoAuthTenantSubappUserService,
															  TenantAppUserAuthorizationService tenantAppUserAuthorizationService) {
		this.cairoAuthTenantSubappUserService = cairoAuthTenantSubappUserService;
		this.tenantAppUserAuthorizationService = tenantAppUserAuthorizationService;
	}

	@Override
	public Authentication convert(TenantSubappUserAuthenticationTokenRequest authRequest) {
		try {
			OAuth2Authorization oAuth2Authorization = tenantAppUserAuthorizationService.findByToken(
				authRequest.getTenantId(),
				authRequest.getAppId(),
				authRequest.getEndpointId(),
				authRequest.getToken(),
				CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN
			);
			OAuth2Authorization.Token<OAuthTenantAppUserAccessToken> tenantAppUserAccessTokenToken = Optional.ofNullable(oAuth2Authorization).map(x -> x.getToken(OAuthTenantAppUserAccessToken.class)).orElse(null);

			if (tenantAppUserAccessTokenToken == null) {
				throw new TokenInvalidException("token错误");
			}

			if (!tenantAppUserAccessTokenToken.isActive()) {
				throw new TokenInvalidException("登录失效");
			}

			if (tenantAppUserAccessTokenToken.isExpired()) {
				throw new TokenExpiredException("登录过期");
			}
			OAuthTenantAppUserAccessToken accessToken = tenantAppUserAccessTokenToken.getToken();
			List<SimpleGrantedAuthority> tokenAuthorities = accessToken.getScopes().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			Collection<GrantedAuthority> userAuthorities;

			String subappId = authRequest.getSubappId();
			String subappVersion = authRequest.getSubappVersion();
			String userId = oAuth2Authorization.getAttribute("userId");


			CairoAuthTenantSubappUser user = cairoAuthTenantSubappUserService.loadTenantSubappUserByUserId(
				authRequest.getTenantId(),
				authRequest.getAppId(),
				authRequest.getEndpointId(),
				subappId,
				subappVersion,
				userId
			);

			CairoOAuthTenantSubappUserPrincipal principal = CairoOAuthTenantSubappUserPrincipal.builder()
				.id(oAuth2Authorization.getId())
				.tenantId(authRequest.getTenantId())
				.appId(authRequest.getAppId())
				.endpointId(authRequest.getEndpointId())
				.subappId(subappId)
				.subappVersion(subappVersion)
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.phoneNumber(user.getPhoneNumber())
				.subappStatus(user.isSubappStatus())
				.appAdmin(user.isAppAdmin())
				.userEnabled(user.isUserEnabled())
				.position(user.getPosition())
				.roles(user.getRoles())
				.departments(user.getDepartments())
				.tags(user.getTags())
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
				throw new TenantAppUserDisabledException();
			}

			if (!principal.isSubappStatus()) {
				throw new TenantSubappNotApplyException();
			}

			userAuthorities = user.getAuthorities();

			List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
			return new CairoOAuthTenantSubappUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
