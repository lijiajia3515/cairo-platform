package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

@Slf4j
public class CairoTenantAppUserPreAuthenticationChecks implements UserDetailsChecker {
	@Setter
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	@Override
	public void check(UserDetails user) {
		if (user instanceof CairoAuthTenantAppUser){
			CairoAuthTenantAppUser authTenantAppUser = (CairoAuthTenantAppUser) user;

			if (!authTenantAppUser.isUserEnabled()) {
				log.debug("Failed to authenticate since user authTenantAppUser is user disabled");
				throw new TenantAppUserDisabledException();
			}

			if (!authTenantAppUser.isAccountNonLocked()) {
				log.debug("Failed to authenticate since user authTenantAppUser is locked");
				throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "账号被锁定"));
			}

			if (!authTenantAppUser.isAccountEnabled()) {
				log.debug("Failed to authenticate since user authTenantAppUser is disabled");
				throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "账号被禁用"));
			}

			if (!authTenantAppUser.isAccountNonExpired()) {
				log.debug("Failed to authenticate since user authTenantAppUser has expired");
				throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "账号已过期"));
			}
		}

	}

}

