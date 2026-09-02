package io.github.lijiajia3515.cairo.auth.framework.security.account;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.DefaultPostAuthenticationChecks;
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
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * cairo account password authentication provider
 */
@Slf4j
@Getter
@Setter
public class CairoAccountPasswordAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthAccountService cairoAuthAccountService;

	private PasswordEncoder passwordEncoder;

	protected boolean hideUserNotFoundExceptions = true;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;

	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.messages, "A message source must be set");
		preAuthenticationChecks = new CairoAccountPreAuthenticationChecks();
		((CairoAccountPreAuthenticationChecks) preAuthenticationChecks).setMessages(messages);
		postAuthenticationChecks = new DefaultPostAuthenticationChecks();
		((DefaultPostAuthenticationChecks) postAuthenticationChecks).setMessages(messages);

		doAfterPropertiesSet();
	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.cairoAuthAccountService, "A authAccountService must be set");
		Assert.notNull(this.passwordEncoder, "A passwordEncoder source must be set");
		Assert.notNull(this.preAuthenticationChecks, "A preAuthenticationChecks must be set");
		Assert.notNull(this.postAuthenticationChecks, "A postAuthenticationChecks must be set");
	}


	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CairoAccountPasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		Assert.isInstanceOf(CairoAccountPasswordAuthenticationToken.class, authentication, () -> this.messages.getMessage("CairoAccountPasswordAuthenticationProvider.onlySupports", "Only CairoAccountPasswordAuthenticationToken is supported"));
		CairoAccountPasswordAuthenticationToken token = (CairoAccountPasswordAuthenticationToken) authentication;

		String username = determineUsername(token);
		CairoAuthAccount user;
		try {
			user = cairoAuthAccountService.loadUserByUsername(username);
			if (user == null) {
				throw new InternalAuthenticationServiceException("CairoAuthAccountService returned null, which is an interface contract violation");
			}
		} catch (AccountNotFoundException ex) {
			log.debug("Failed to find user '" + username + "'");
			if (!this.hideUserNotFoundExceptions) {
				throw ex;
			}
			throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}
		Assert.notNull(user, "user returned null - a violation of the interface contract");

		this.preAuthenticationChecks.check(user);
		additionalAuthenticationChecks(user, token);
		this.postAuthenticationChecks.check(user);

		Object principal = user;
		if (this.forcePrincipalAsString) {
			principal = user.getId();
		}

		return createSuccessAuthentication(principal, authentication, user);
	}


	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 CairoAuthAccount authAccount) {
		// Ensure we return the original credentials the authAccount supplied,
		// so subsequent attempts are successful even with encoded passwords.
		// Also ensure we return the original getDetails(), so that future
		// authentication events after cache expiry contain the details
		UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(authAccount.getAuthorities()));
		result.setDetails(authentication.getDetails());
		log.debug("Authenticated account");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthAccount account, CairoAccountPasswordAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getCredentials() == null) {
			log.debug("Failed to authenticate since no credentials provided");
			throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}
		String presentedPassword = authentication.getCredentials().toString();
		if (!this.passwordEncoder.matches(presentedPassword, account.getPassword())) {
			log.debug("Failed to authenticate since password does not match stored value");
			throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
		}
	}


	private String determineUsername(CairoAccountPasswordAuthenticationToken authentication) {
		return (authentication.getUsername() == null) ? "NONE_PROVIDED" : authentication.getUsername();
	}

}
