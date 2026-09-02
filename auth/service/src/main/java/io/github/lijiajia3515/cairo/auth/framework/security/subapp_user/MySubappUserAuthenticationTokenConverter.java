package io.github.lijiajia3515.cairo.auth.framework.security.subapp_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
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

public class MySubappUserAuthenticationTokenConverter implements SubappUserAuthenticationTokenConverter {
	private final CairoAuthSubappUserService cairoAuthSubappUserService;
	private final AppUserAuthorizationService appUserAuthorizationService;

	public MySubappUserAuthenticationTokenConverter(CairoAuthSubappUserService cairoAuthSubappUserService,
														AppUserAuthorizationService appUserAuthorizationService) {
		this.cairoAuthSubappUserService = cairoAuthSubappUserService;
		this.appUserAuthorizationService = appUserAuthorizationService;
	}

	@Override
	public Authentication convert(SubappUserAuthenticationTokenRequest tokenRequest) {
		try {
			OAuth2Authorization oAuth2Authorization = appUserAuthorizationService.findByToken(
				tokenRequest.getAppId(),
				tokenRequest.getEndpointId(),
				tokenRequest.getToken(),
				CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN
			);
			OAuth2Authorization.Token<OAuthAppUserAccessToken> token = Optional.ofNullable(oAuth2Authorization).map(x -> x.getToken(OAuthAppUserAccessToken.class)).orElse(null);

			if (token == null) {
				throw new TokenInvalidException("token错误");
			}

			if (!token.isActive()) {
				throw new TokenInvalidException("登录失效");
			}

			if (token.isExpired()) {
				throw new TokenExpiredException("登录过期");
			}
			OAuthAppUserAccessToken accessToken = token.getToken();
			List<SimpleGrantedAuthority> tokenAuthorities = accessToken.getScopes().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			Collection<GrantedAuthority> userAuthorities;

			String userId = oAuth2Authorization.getAttribute("userId");

			CairoAuthSubappUser user = cairoAuthSubappUserService.loadSubappUserByAppUserId(tokenRequest.getAppId(), tokenRequest.getEndpointId(), tokenRequest.getSubappId(), tokenRequest.getSubappVersion(), userId);

			CairoOAuthSubappUserPrincipal principal = CairoOAuthSubappUserPrincipal.builder()
				.id(oAuth2Authorization.getId())
				.appId(tokenRequest.getAppId())
				.endpointId(tokenRequest.getEndpointId())
				.subappId(tokenRequest.getSubappId())
				.subappVersion(tokenRequest.getSubappVersion())
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.phoneNumber(user.getPhoneNumber())
				.roles(user.getRoles())
				.position(user.getPosition())
				.departments(user.getDepartments())
				.tags(user.getTags())
				.appAdmin(user.isAppAdmin())
				.userEnabled(user.isUserEnabled())
				.subappStatus(user.isSubappStatus())
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
				throw new AppUserDisabledException();
			}

			if (!principal.isSubappStatus()) {
				throw new SubappNotApplyException();
			}

			userAuthorities = user.getAuthorities();
			List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, userAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
			return new CairoOAuthSubappUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
