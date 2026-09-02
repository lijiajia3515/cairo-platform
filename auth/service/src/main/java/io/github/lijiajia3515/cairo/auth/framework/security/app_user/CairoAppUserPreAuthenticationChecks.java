package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
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
public class CairoAppUserPreAuthenticationChecks implements UserDetailsChecker {
	@Setter
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	@Override
	public void check(UserDetails user) {
		if (user instanceof CairoAuthAppUser){
			CairoAuthAppUser authAppUser = (CairoAuthAppUser) user;

			if (!authAppUser.isUserEnabled()) {
				log.debug("Failed to authenticate since user authAppUser is user disabled");
				throw new AppUserDisabledException();
			}

			if (!authAppUser.isAccountNonLocked()) {
				log.debug("Failed to authenticate since user authAppUser is locked");
				throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "账号被锁定"));
			}

			if (!authAppUser.isAccountEnabled()) {
				log.debug("Failed to authenticate since user authAppUser is disabled");
				throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "账号被禁用"));
			}

			if (!authAppUser.isAccountNonExpired()) {
				log.debug("Failed to authenticate since user authAppUser has expired");
				throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "账号已过期"));
			}
		}

	}

}

