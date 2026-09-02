
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * An {@link Authentication} that contains a
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
 * Token</a>.
 *
 * Used by {@link BearerTokenAuthenticationFilter} to prepare an authentication attempt
 * and supported by {@link JwtAuthenticationProvider}.
 *
 * @author Josh Cummings
 * @since 5.1
 */
@Getter
public class AccountTokenAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	/**
	 * token
	 */
	private final String token;

	/**
	 * Create a {@code AccountTokenAuthenticationToken} using the provided parameter(s)
	 * @param token - the bearer token
	 */
	public AccountTokenAuthenticationToken(String token) {
		super(Collections.emptyList());
		Assert.hasText(token, "token cannot be empty");
		this.token = token;
	}

	@Override
	public Object getCredentials() {
		return this.getToken();
	}

	@Override
	public Object getPrincipal() {
		return this.getToken();
	}

}
