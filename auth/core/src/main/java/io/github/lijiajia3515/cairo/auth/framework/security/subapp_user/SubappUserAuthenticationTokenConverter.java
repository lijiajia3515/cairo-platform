package io.github.lijiajia3515.cairo.auth.framework.security.subapp_user;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface SubappUserAuthenticationTokenConverter {

	Authentication convert(SubappUserAuthenticationTokenRequest tokenRequest);

}
