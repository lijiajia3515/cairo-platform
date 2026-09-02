package io.github.lijiajia3515.cairo.auth.framework.security.event;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.HashMap;
import java.util.Map;

public class CairoAuthenticationEventPublisher extends DefaultAuthenticationEventPublisher {
	public CairoAuthenticationEventPublisher() {
		super();
		additionalExceptionMapping();
	}

	public CairoAuthenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		super(applicationEventPublisher);
		additionalExceptionMapping();
	}

	protected void additionalExceptionMapping() {
		Map<Class<? extends AuthenticationException>, Class<? extends AbstractAuthenticationFailureEvent>> mappings = new HashMap<>();
		// 密码异常
		mappings.put(VerifyCodeBadCredentialsException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(SnsCodeFailedException.class, AuthenticationFailureBadCredentialsEvent.class);
		// 账号
		mappings.put(AccountNotFoundException.class, AuthenticationFailureBadCredentialsEvent.class);

		// 应用异常
		mappings.put(AppNotFoundException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(AppDisabledException.class, AuthenticationFailureBadCredentialsEvent.class);

		// 端异常
		mappings.put(EndpointNotFoundException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(EndpointDisabledException.class, AuthenticationFailureBadCredentialsEvent.class);

		// 企业异常
		mappings.put(TenantNotFoundException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(TenantDisabledException.class, AuthenticationFailureBadCredentialsEvent.class);

		// 企业应用异常
		mappings.put(TenantAppNotApplyException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(TenantAppDisabledException.class, AuthenticationFailureBadCredentialsEvent.class);

		// 用户异常
		mappings.put(TenantAppUserNotFoundException.class, AuthenticationFailureBadCredentialsEvent.class);
		mappings.put(TenantAppUserDisabledException.class, AuthenticationFailureBadCredentialsEvent.class);

		// oauth2 异常
		mappings.put(OAuth2AuthenticationException.class, AuthenticationFailureBadCredentialsEvent.class);
		setAdditionalExceptionMappings(mappings);
	}
}
