package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

/**
 * 应用用户账号授权模式 authentication provider
 * <p>
 * 使用账号级 access_token 置换应用用户 token 时，根据账号ID加载应用用户
 */
@Slf4j
@Getter
@Setter
public class CairoAppUserAccountAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthAppUserService cairoAuthAppUserService;

	protected boolean hideUserNotFoundExceptions = true;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;

	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

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
		Assert.isInstanceOf(CairoAppUserAccountAuthenticationToken.class, authentication,
			() -> this.messages.getMessage("CairoAppUserAccountAuthenticationProvider.onlySupports",
				"Only CairoAppUserAccountAuthenticationToken is supported"));
		CairoAppUserAccountAuthenticationToken token = (CairoAppUserAccountAuthenticationToken) authentication;

		CairoAuthAppUser user = null;
		try {
			user = cairoAuthAppUserService.loadAppUserByAccountId(token.getLoginType(), token.getAppId(), token.getEndpointId(), token.getClientId(), token.getAccountId());
			if (user == null) {
				throw new InternalAuthenticationServiceException(
					"cairoAuthAppUserService returned null, which is an interface contract violation");
			}
		} catch (AppNotFoundException | AppDisabledException
	             | EndpointNotFoundException | EndpointDisabledException
	             | AccountNotFoundException | AppUserNotFoundException e) {
			log.debug("Failed to find use '" + token + "'");
			if (!this.hideUserNotFoundExceptions) {
				throw e;
			}
			throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码错误"));
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
		return CairoAppUserAccountAuthenticationToken.class.isAssignableFrom(authentication);
	}

	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 UserDetails user) {
		UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal,
			authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
		result.setDetails(authentication.getDetails());
		log.debug("Authenticated app user");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthAppUser user, CairoAppUserAccountAuthenticationToken authentication) throws AuthenticationException {

	}

}
