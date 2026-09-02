package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface TenantSubappUserAuthenticationTokenConverter {

	Authentication convert(TenantSubappUserAuthenticationTokenRequest authenticationToken);

}
