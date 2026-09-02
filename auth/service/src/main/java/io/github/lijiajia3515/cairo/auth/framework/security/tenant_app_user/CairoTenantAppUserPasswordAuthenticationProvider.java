package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.DefaultPostAuthenticationChecks;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * cairo tenant app endpoint user password authentication provider
 */
@Slf4j
@Setter
@Getter
public class CairoTenantAppUserPasswordAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthTenantAppUserService cairoAuthTenantAppUserService;

	private PasswordEncoder passwordEncoder;

	protected boolean hideUserNotFoundExceptions = true;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;
	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.messages, "A message source must be set");
		preAuthenticationChecks = new CairoTenantAppUserPreAuthenticationChecks();
		((CairoTenantAppUserPreAuthenticationChecks) preAuthenticationChecks).setMessages(messages);
		postAuthenticationChecks = new DefaultPostAuthenticationChecks();
		((DefaultPostAuthenticationChecks) postAuthenticationChecks).setMessages(messages);

		doAfterPropertiesSet();
	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.cairoAuthTenantAppUserService, "A cairoAuthEndpointUserService must be set");
		Assert.notNull(this.passwordEncoder, "A passwordEncoder source must be set");
		Assert.notNull(this.preAuthenticationChecks, "A preAuthenticationChecks must be set");
		Assert.notNull(this.postAuthenticationChecks, "A postAuthenticationChecks must be set");
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(CairoTenantAppUserPasswordAuthenticationToken.class, authentication,
			() -> this.messages.getMessage("CairoTenantAppUserPasswordAuthenticationProvider.onlySupports",
				"Only CairoTenantAppUserPasswordAuthenticationToken is supported"));
		CairoTenantAppUserPasswordAuthenticationToken token = (CairoTenantAppUserPasswordAuthenticationToken) authentication;


		CairoAuthTenantAppUser user = null;
		try {
			user = cairoAuthTenantAppUserService.loadTenantAppUserByUsername(LoginType.PASSWORD, token.getTenantId(), token.getAppId(), token.getEndpointId(), token.getClientId(), token.getUsername());
			if (user == null) {
				throw new InternalAuthenticationServiceException("cairoAuthEndpointUserService returned null, which is an interface contract violation");
			}
		} catch (TenantNotFoundException | TenantDisabledException | AppNotFoundException | TenantAppNotApplyException |
                 AppDisabledException | AccountNotFoundException | TenantAppUserNotFoundException e) {
			log.debug("Failed to find use '" + token + "'");
			if (!this.hideUserNotFoundExceptions) {
				throw e;
			}
			throw new BadCredentialsException(this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}

		this.preAuthenticationChecks.check(user);
		additionalAuthenticationChecks(user, token);
		this.postAuthenticationChecks.check(user);

		Object principal = user;
		if (this.forcePrincipalAsString) {
			principal = user.getId();
		}

		return createSuccessAuthentication(principal, authentication, user);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CairoTenantAppUserPasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 UserDetails user) {
		// Ensure we return the original credentials the user supplied,
		// so subsequent attempts are successful even with encoded passwords.
		// Also ensure we return the original getDetails(), so that future
		// authentication events after cache expiry contain the details
		UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal,
			authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
		result.setDetails(authentication.getDetails());
		log.debug("Authenticated tenant app endpoint user");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthTenantAppUser user, CairoTenantAppUserPasswordAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getCredentials() == null || authentication.getCredentials().isBlank()) {
			log.debug("Failed to authenticate since no credentials provided");
			throw new BadCredentialsException(this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.emptyCredentials", "密码不能未空"));
		}
		if (user.getAccountPassword() == null || user.getAccountPassword().isBlank()) {
			log.debug("user password is empty");
			throw new BadCredentialsException(this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}
		String presentedPassword = authentication.getCredentials();
		try {
			if (!this.passwordEncoder.matches(presentedPassword, user.getAccountPassword())) {
				log.debug("Failed to authenticate since password does not match stored value");
				throw new BadCredentialsException(this.messages
					.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
			}
		} catch (RuntimeException e) {
			log.debug("Failed valid password", e);
			throw new BadCredentialsException(this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}
	}
}
