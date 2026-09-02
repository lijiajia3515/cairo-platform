package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

@Slf4j
public class DefaultPreAuthenticationChecks implements UserDetailsChecker {
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	@Override
	public void check(UserDetails user) {
		if (!user.isAccountNonLocked()) {
			log.debug("Failed to authenticate since user account is locked");
			throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "账号被锁定"));
		}
		if (!user.isEnabled()) {
			log.debug("Failed to authenticate since user account is disabled");
			throw new DisabledException(messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "账号被禁用"));
		}
		if (!user.isAccountNonExpired()) {
			log.debug("Failed to authenticate since user account has expired");
			throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "账号已过期"));
		}
	}

}

