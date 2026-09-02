package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import org.springframework.security.core.GrantedAuthority;
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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;

public class SimpleAccountAuthenticationConverter implements CairoJwtAuthenticationConverter {

	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

	public SimpleAccountAuthenticationConverter() {
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
	}

	@Override
	public CairoOAuthAccountAuthenticationToken convert(Jwt jwt) {
		String type = jwt.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
		if (!ACCOUNT.getValue().equals(type)) {
			return null;
		}
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

		CairoOAuthAccountPrincipal principal = CairoOAuthAccountPrincipal.builder()
			.id(accountTokenId)
			.loginType(loginType)
			.snsType(snsType)
			.appId(appId)
			.clientId(clientId)
			.accountId(accountId)
			.build();

		return new CairoOAuthAccountAuthenticationToken(token, principal, tokenAuthorities);
	}
}
