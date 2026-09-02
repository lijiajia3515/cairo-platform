package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface AppUserAuthenticationTokenConverter {

	Authentication convert(AppUserAuthenticationTokenRequest tokenRequest);

}
