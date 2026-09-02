package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.APP_USER;

public class SimpleAppUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

	public SimpleAppUserAuthenticationConverter() {
		grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthorityPrefix("");
	}

	@Override
	public CairoOAuthAppUserAuthenticationToken convert(Jwt jwt) {
		String type = jwt.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
		if (!APP_USER.getValue().equals(type)) {
			return null;
		}

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

		CairoOAuthAppUserPrincipal principal = CairoOAuthAppUserPrincipal.builder()
			.id(tokenId)
			.loginType(loginType)
			.snsType(snsType)
			.appId(appId)
			.endpointId(endpointId)
			.clientId(clientId)
			.userId(userId)
			.build();

		return new CairoOAuthAppUserAuthenticationToken(token, principal, tokenAuthorities);
	}

}
