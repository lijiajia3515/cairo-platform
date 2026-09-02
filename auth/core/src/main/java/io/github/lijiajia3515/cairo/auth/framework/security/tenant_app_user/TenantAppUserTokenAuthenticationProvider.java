

package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public final class TenantAppUserTokenAuthenticationProvider implements AuthenticationProvider {

	private final Log logger = LogFactory.getLog(getClass());


	private TenantAppUserAuthenticationTokenConverter authenticationConverter = null;


	/**
	 * Introspect and validate the opaque
	 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
	 * Token</a> and then delegates {@link org.springframework.security.core.Authentication} instantiation to
	 * {@link org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter}.
	 * <p>
	 * If created Authentication is instance of {@link org.springframework.security.authentication.AbstractAuthenticationToken} and
	 * details are null, then introspection result details are used.
	 *
	 * @param authentication the authentication request object.
	 * @return A successful authentication
	 * @throws org.springframework.security.core.AuthenticationException if authentication failed for some reason
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof TenantAppUserAuthenticationTokenRequest token)) {
			return null;
		}
		Authentication result = this.authenticationConverter.convert(token);
		if (result == null) {
			return null;
		}
		if (AbstractAuthenticationToken.class.isAssignableFrom(result.getClass())) {
			final AbstractAuthenticationToken auth = (AbstractAuthenticationToken) result;
			if (auth.getDetails() == null) {
				auth.setDetails(token.getDetails());
			}
		}
		this.logger.debug("Authenticated token");
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return TenantAppUserAuthenticationTokenRequest.class.isAssignableFrom(authentication);
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
	 * {@link org.springframework.security.core.Authentication} instance of your choice. By default,
	 * {@link org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication} will be built.
	 *
	 * @param authenticationConverter the converter to use
	 * @since 5.8
	 */
	public void setAuthenticationConverter(TenantAppUserAuthenticationTokenConverter authenticationConverter) {
		Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
		this.authenticationConverter = authenticationConverter;
	}

}
