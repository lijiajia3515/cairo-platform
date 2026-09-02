//package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;
//
//import io.github.lijiajia3515.cairo.auth.framework.security.account.AccountTokenAuthenticationConverter;
//import io.github.lijiajia3515.cairo.auth.framework.security.authentication.account.CairoAuthAccount;
//import io.github.lijiajia3515.cairo.auth.framework.security.authentication.account.CairoAuthAccountService;
//import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
//import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
//import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
//import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
//import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class CairoAccountTokenAuthenticationConverter implements AccountTokenAuthenticationConverter {
//	private final CairoAuthAccountService cairoAuthAccountService;
//
//	public CairoAccountTokenAuthenticationConverter(CairoAuthAccountService cairoAuthAccountService) {
//		this.cairoAuthAccountService = cairoAuthAccountService;
//	}
//
//	@Override
//	public CairoOAuthAccountAuthenticationToken convert(String introspectedToken) {
//		List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
//		Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());
//
//		String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
//		LoginType loginType = Optional.ofNullable(loginTypeValue).map(LoginType::new).orElse(LoginType.UNKNOWN);
//		String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
//		String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
//		String accountId = jwt.getClaimAsString(CairoOAuthParameterNames.ACCOUNT_ID);
//		String accountTokenId = jwt.getSubject();
//
//		OAuthAccountAccessToken token = new OAuthAccountAccessToken(
//			OAuth2AccessToken.TokenType.BEARER,
//			accountId,
//			accountTokenId,
//			jwt.getTokenValue(),
//			jwt.getIssuedAt(),
//			jwt.getExpiresAt(),
//			scopes,
//			jwt.getClaims()
//		);
//		Collection<GrantedAuthority> jwtAuthorities = grantedAuthoritiesConverter.convert(jwt);
//
//		CairoOAuthAccountPrincipal principal;
//		Collection<GrantedAuthority> accountAuthorities;
//		try {
//			CairoAuthAccount account = cairoAuthAccountService.loadAccountByAccountId(loginType, accountId);
//			principal = CairoOAuthAccountPrincipal.builder()
//				.id(accountTokenId)
//				.loginType(loginType)
//				.appId(appId)
//				.clientId(clientId)
//				.accountId(accountId)
//				.nickname(account.getNickname())
//				.username(account.getLoginname())
//				.phoneNumber(account.getPhoneNumber())
//				.email(account.getEmail())
//				.avatarUrl(account.getAvatarUrl())
//				.roles(Collections.emptyList())
//				.departments(Collections.emptyList())
//				.tags(Collections.emptyList())
//				.locked(account.isLocked())
//				.enabled(account.isEnabled())
//				.build();
//
//			if (principal.getLocked()) {
//				throw new LockedException("账号已锁定");
//			}
//			if (!principal.getEnabled()) {
//				throw new DisabledException("账号已禁用");
//			}
//
//			accountAuthorities = account.getAuthorities();
//		} catch (AuthenticationException e) {
//			throw e;
//		} catch (RuntimeException ex) {
//			throw new AuthenticationServiceException("认证服务故障", ex);
//		}
//
//		List<GrantedAuthority> authorities = Stream.of(jwtAuthorities, accountAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
//		return new CairoOAuthAccountAuthenticationToken(token, principal, authorities);
//	}
//}
