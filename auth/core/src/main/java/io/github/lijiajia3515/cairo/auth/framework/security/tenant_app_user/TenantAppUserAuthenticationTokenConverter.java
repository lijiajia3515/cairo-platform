package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface TenantAppUserAuthenticationTokenConverter {

	Authentication convert(TenantAppUserAuthenticationTokenRequest token);

}
