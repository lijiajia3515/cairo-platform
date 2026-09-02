package io.github.lijiajia3515.cairo.auth.framework.security.account;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyAccountAuthenticationTokenConverter implements AccountAuthenticationTokenConverter {
	private final CairoAuthAccountService cairoAuthAccountService;
	private final AccountAuthorizationService accountAuthorizationService;

	public MyAccountAuthenticationTokenConverter(CairoAuthAccountService cairoAuthAccountService, AccountAuthorizationService accountAuthorizationService) {
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.accountAuthorizationService = accountAuthorizationService;
	}

	@Override
	public Authentication convert(String introspectedToken) {
		try {
			OAuth2Authorization oAuth2Authorization = accountAuthorizationService.findByToken(introspectedToken, CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN);
			OAuth2Authorization.Token<OAuthAccountAccessToken> token = Optional.ofNullable(oAuth2Authorization).map(x->x.getToken(OAuthAccountAccessToken.class)).orElse(null);
			if (token == null) {
				throw new TokenInvalidException("token错误");
			}

			if (!token.isActive()) {
				throw new TokenInvalidException("登录失效");
			}

			if (token.isExpired()) {
				throw new TokenExpiredException("登录过期");
			}
			OAuthAccountAccessToken accessToken = token.getToken();
			List<SimpleGrantedAuthority> tokenAuthorities = accessToken.getScopes().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			CairoOAuthAccountPrincipal principal;
			Collection<GrantedAuthority> accountAuthorities;

			CairoAuthAccount account = cairoAuthAccountService.loadAccountByAccountId(LoginType.UNKNOWN, accessToken.getAccountId());
			principal = CairoOAuthAccountPrincipal.builder()
				.id(oAuth2Authorization.getId())
				.loginType(Optional.<String>ofNullable(oAuth2Authorization.getAttribute("loginType")).map(LoginType::new).orElse(LoginType.UNKNOWN))
				.snsType(oAuth2Authorization.getAttribute("snsType"))
				.appId(oAuth2Authorization.getAttribute("appId"))
				.clientId(oAuth2Authorization.getAttribute("clientId"))
				.accountId(account.getAccountId())
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

			List<GrantedAuthority> authorities = Stream.of(tokenAuthorities, accountAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
			return new CairoOAuthAccountAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
