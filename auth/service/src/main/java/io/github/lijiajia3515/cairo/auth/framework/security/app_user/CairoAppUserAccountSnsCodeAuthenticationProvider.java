package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.DefaultPostAuthenticationChecks;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserPreAuthenticationChecks;
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
 * cairo app endpoint user account sns code authentication provider
 */
@Slf4j
@Setter
@Getter
public class CairoAppUserAccountSnsCodeAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthAppUserService cairoAuthAppUserService;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;

	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

	public CairoAppUserAccountSnsCodeAuthenticationProvider() {
	}

	public CairoAppUserAccountSnsCodeAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService) {
		this.cairoAuthAppUserService = cairoAuthAppUserService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.messages, "A message source must be set");
		preAuthenticationChecks = new CairoAppUserPreAuthenticationChecks();
		((CairoAppUserPreAuthenticationChecks) preAuthenticationChecks).setMessages(messages);
		postAuthenticationChecks = new DefaultPostAuthenticationChecks();
		((DefaultPostAuthenticationChecks) postAuthenticationChecks).setMessages(messages);
		doAfterPropertiesSet();
	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.cairoAuthAppUserService, "A cairoAuthAppUserService must be set");
		Assert.notNull(this.preAuthenticationChecks, "A preAuthenticationChecks must be set");
		Assert.notNull(this.postAuthenticationChecks, "A postAuthenticationChecks must be set");
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		Assert.isInstanceOf(CairoAppUserAccountSnsCodeAuthenticationToken.class, authentication,
			() -> this.messages.getMessage("CairoAppUserAccountSnsCodeAuthenticationToken.onlySupports",
				"Only CairoAppUserAccountSnsCodeAuthenticationToken is supported"));
		CairoAppUserAccountSnsCodeAuthenticationToken token = (CairoAppUserAccountSnsCodeAuthenticationToken) authentication;

		CairoAuthAppUser user = null;
		try {
			user = cairoAuthAppUserService.loadAppUserByAccountSns(token.getAppId(), token.getEndpointId(), token.getClientId(), token.getSnsType(), token.getSnsProviderId(), token.getSnsCode());
		} catch (SnsAuthenticationNotSupportException exception) {
			throw new InternalAuthenticationServiceException(exception.getMessage());
		} catch (Exception e) {
			throw new SnsCodeFailedException(e.getMessage());
		}

		if (user == null) {
			throw new InternalAuthenticationServiceException(
				"CairoAppUserAccountSnsCodeAuthenticationToken returned null, which is an interface contract violation");
		}

		Assert.notNull(user, "authAppUser returned null - a violation of the interface contract");

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
		return CairoAppUserAccountSnsCodeAuthenticationToken.class.isAssignableFrom(authentication);
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
		log.debug("Authenticated app endpoint user");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthAppUser user, CairoAppUserAccountSnsCodeAuthenticationToken authentication) throws AuthenticationException {

	}

}
