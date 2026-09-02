package io.github.lijiajia3515.cairo.auth.framework.security.account;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface AccountAuthenticationTokenConverter {

	/**
	 * Converts a successful introspection result into an authentication result.
	 *
	 * @param introspectedToken the bearer token used to perform token introspection
	 * @return an {@link Authentication} instance
	 */
	Authentication convert(String introspectedToken);

}
