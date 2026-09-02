package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.DefaultPostAuthenticationChecks;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
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
 * cairo app endpoint user verify code authentication provider
 */
@Slf4j
@Getter
@Setter
public class CairoAppUserVerifyCodeAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private CairoAuthAppUserService cairoAuthAppUserService;

	private VerifyCodeService verifyCodeService;

	protected boolean hideUserNotFoundExceptions = true;

	private boolean forcePrincipalAsString = false;
	private UserDetailsChecker preAuthenticationChecks;

	private UserDetailsChecker postAuthenticationChecks;

	private GrantedAuthoritiesMapper authoritiesMapper = new SimpleAuthorityMapper();

	public CairoAppUserVerifyCodeAuthenticationProvider() {
	}

	public CairoAppUserVerifyCodeAuthenticationProvider(CairoAuthAppUserService cairoAuthAppUserService, VerifyCodeService verifyCodeService) {
		this.cairoAuthAppUserService = cairoAuthAppUserService;
		this.verifyCodeService = verifyCodeService;
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
		Assert.notNull(this.cairoAuthAppUserService, "a cairoAuthAppUserService must be set");
		Assert.notNull(this.verifyCodeService, "a verifyCodeService must be set");
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(CairoAppUserVerifyCodeAuthenticationToken.class, authentication,
			() -> this.messages.getMessage("CairoAppUserVerifyCodeAuthenticationProvider.onlySupports", "Only CairoAppUserVerifyCodeAuthenticationToken is supported"));
		CairoAppUserVerifyCodeAuthenticationToken token = (CairoAppUserVerifyCodeAuthenticationToken) authentication;


		CairoAuthAppUser user = null;
		try {
			user = cairoAuthAppUserService.loadAppUserByPhoneNumber(LoginType.VERIFY_CODE, token.getAppId(), token.getEndpointId(), token.getClientId(), token.getPhoneNumber());
			if (user == null) {
				throw new InternalAuthenticationServiceException("CairoAppUserVerifyCodeAuthenticationToken returned null, which is an interface contract violation");
			}
		} catch (AppNotFoundException | AppDisabledException |
				 AccountNotFoundException | EndpointNotFoundException e) {
			log.debug("Failed to find use '" + token + "'");
			if (!this.hideUserNotFoundExceptions) {
				throw e;
			}

			throw new VerifyCodeBadCredentialsException(this.messages.getMessage("CairoAppUserVerifyCodeAuthenticationProvider.badVerifyCode", "验证码错误"));
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
		return CairoAppUserVerifyCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 UserDetails user) {
		// Ensure we return the original credentials the user supplied,
		// so subsequent attempts are successful even with encoded passwords.
		// Also ensure we return the original getDetails(), so that future
		// authentication events after cache expiry contain the details
		UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
		result.setDetails(authentication.getDetails());
		log.debug("Authenticated app endpoint user");
		return result;
	}

	protected void additionalAuthenticationChecks(CairoAuthAppUser user, CairoAppUserVerifyCodeAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getCredentials() == null) {
			log.debug("Failed to authenticate since no credentials provided");
			throw new VerifyCodeBadCredentialsException(this.messages.getMessage("CairoAppUserVerifyCodeAuthenticationProvider.emptyVerifyCode", "验证码不能未空"));
		}

		VerifyVerifyCodeArgs verifyArgs = VerifyVerifyCodeArgs.builder()
			.bizCode(CairoAuthVerifyCodeConstants.AUTH)
			.target(authentication.getPhoneNumber())
			.verifyCode(authentication.getVerifyCode())
			.build();

		VerifyCodeStat verifyStat = verifyCodeService.verify(verifyArgs);
		log.info("verifyCode stat: {}", verifyStat);
		if (!VerifyCodeStat.SUCCESS.equals(verifyStat)) {
			log.debug("Failed to authenticate since verifyCode does not match stored value");
			throw new VerifyCodeBadCredentialsException(this.messages.getMessage("CairoAppUserVerifyCodeAuthenticationProvider.badVerifyCode", "验证码错误"));
		}
	}


	private String determinePhoneNumber(CairoAccountVerifyCodeAuthenticationToken authentication) {
		return (authentication.getPhoneNumber() == null) ? "NONE_PROVIDED" : authentication.getPhoneNumber();
	}

}
