package io.github.lijiajia3515.cairo.auth.framework.security.account;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.DefaultPostAuthenticationChecks;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationNotSupportException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

/**
 * cairo account sns code authentication provider
 */
@Slf4j
@Setter
@Getter
public class CairoAccountSnsCodeAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthAccountService cairoAuthAccountService;

	protected boolean hideUserNotFoundExceptions = true;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;

	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

	public CairoAccountSnsCodeAuthenticationProvider() {
	}

	public CairoAccountSnsCodeAuthenticationProvider(CairoAuthAccountService cairoAuthAccountService) {
		this.cairoAuthAccountService = cairoAuthAccountService;
	}

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
		Assert.notNull(this.cairoAuthAccountService, "A cairoAuthAccountService must be set");
		Assert.notNull(this.preAuthenticationChecks, "A preAuthenticationChecks must be set");
		Assert.notNull(this.postAuthenticationChecks, "A postAuthenticationChecks must be set");
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		Assert.isInstanceOf(CairoAccountSnsCodeAuthenticationToken.class, authentication,
			() -> this.messages.getMessage("CairoAccountSnsCodeAuthenticationToken.onlySupports",
				"Only CairoAccountSnsCodeAuthenticationToken is supported"));
		CairoAccountSnsCodeAuthenticationToken token = (CairoAccountSnsCodeAuthenticationToken) authentication;

		CairoAuthAccount user = null;
		try {
			user = cairoAuthAccountService.loadAccountBySnsCode(token.getSnsType(), token.getSnsProviderId(), token.getSnsCode());
		} catch (SnsAuthenticationNotSupportException exception) {
			throw new InternalAuthenticationServiceException(exception.getMessage());
		} catch (Exception e) {
			throw new SnsCodeFailedException(e.getMessage());
		}
		if (user == null) {
			if (hideUserNotFoundExceptions) {
				throw new SnsCodeFailedException("登录失败（未绑定登录账号）");
			} else {
				throw new AccountNotFoundException();
			}
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
		return CairoAccountSnsCodeAuthenticationToken.class.isAssignableFrom(authentication);
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
		log.debug("Authenticated account user");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthAccount account, CairoAccountSnsCodeAuthenticationToken authentication) throws AuthenticationException {

	}

}
