

package io.github.lijiajia3515.cairo.auth.framework.security.account;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.util.Assert;

public final class AccountTokenAuthenticationProvider implements AuthenticationProvider {

	private final Log logger = LogFactory.getLog(getClass());


	private AccountAuthenticationTokenConverter authenticationConverter = null;


	/**
	 * Introspect and validate the opaque
	 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
	 * Token</a> and then delegates {@link Authentication} instantiation to
	 * {@link OpaqueTokenAuthenticationConverter}.
	 * <p>
	 * If created Authentication is instance of {@link AbstractAuthenticationToken} and
	 * details are null, then introspection result details are used.
	 *
	 * @param authentication the authentication request object.
	 * @return A successful authentication
	 * @throws AuthenticationException if authentication failed for some reason
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof AccountAuthenticationTokenRequest account)) {
			return null;
		}
		Authentication result = this.authenticationConverter.convert(account.getToken());
		if (result == null) {
			return null;
		}
		if (AbstractAuthenticationToken.class.isAssignableFrom(result.getClass())) {
			final AbstractAuthenticationToken auth = (AbstractAuthenticationToken) result;
			if (auth.getDetails() == null) {
				auth.setDetails(account.getDetails());
			}
		}
		this.logger.debug("Authenticated token");
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return AccountAuthenticationTokenRequest.class.isAssignableFrom(authentication);
	}

//	/**
//	 * Default {@link OpaqueTokenAuthenticationConverter}.
//	 * @param introspectedToken the bearer string that was successfully introspected
//	 * @param authenticatedPrincipal the successful introspection output
//	 * @return a {@link BearerTokenAuthentication}
//	 */
//	static CairoOAuthAccountAuthenticationToken convert(String introspectedToken,
//														OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
//		Instant iat = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.IAT);
//		Instant exp = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.EXP);
//		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken,
//				iat, exp);
//		return new CairoOAuthAccountAuthenticationToken(authenticatedPrincipal, accessToken,
//				authenticatedPrincipal.getAuthorities());
//	}

	/**
	 * Provide with a custom bean to turn successful introspection result into an
	 * {@link Authentication} instance of your choice. By default,
	 * {@link BearerTokenAuthentication} will be built.
	 *
	 * @param authenticationConverter the converter to use
	 * @since 5.8
	 */
	public void setAuthenticationConverter(AccountAuthenticationTokenConverter authenticationConverter) {
		Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
		this.authenticationConverter = authenticationConverter;
	}

}
