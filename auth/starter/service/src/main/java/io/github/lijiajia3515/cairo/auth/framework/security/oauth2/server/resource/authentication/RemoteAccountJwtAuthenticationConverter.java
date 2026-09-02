package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CairoAccountAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountAuthArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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

@Slf4j
public class RemoteAccountJwtAuthenticationConverter implements CairoJwtAuthenticationConverter {
	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
	private final AccountClientApiService accountClientApiService;

	public RemoteAccountJwtAuthenticationConverter(AccountClientApiService accountClientApiService) {
		this.accountClientApiService = accountClientApiService;
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String type = jwt.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
		if (!ACCOUNT.getValue().equals(type)) {
			return null;
		}

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

		Collection<GrantedAuthority> jwtAuthority = grantedAuthoritiesConverter.convert(jwt);

		CairoOAuthAccountPrincipal principal;
		Collection<GrantedAuthority> accountAuthorities;
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
					// 客户端异常
				} else if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException();
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException();
					// 账号异常
				} else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException();
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException("账号被锁定");
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException("账号被禁用");
				}
				// 其他异常
				else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			CairoOAuthAccountPrincipal account = model.getPrincipal();
			principal = CairoOAuthAccountPrincipal.builder()
				.id(accountTokenId)
				.loginType(loginType)
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

		List<GrantedAuthority> authorities = Stream.of(jwtAuthority, accountAuthorities).flatMap(Collection::stream).collect(Collectors.toList());
		return new CairoOAuthAccountAuthenticationToken(token, principal, authorities);
	}

}
