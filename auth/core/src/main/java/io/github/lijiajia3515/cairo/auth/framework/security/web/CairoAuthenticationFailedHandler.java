package io.github.lijiajia3515.cairo.auth.framework.security.web;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.ServiceBusiness;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.http.converter.AbstractHttpMessageHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * AuthenticationException:认证异常的父类，抽象类
 * BadCredentialsException:登录凭证（密码）异常
 * InsufficientAuthenticationException:登陆凭证不够充分而抛出的异常
 * SessionAuthenticationException:会话并发管理时抛出的异常，例如会话总数超出最大限制数
 * UsernameNotFoundException:用户名不存在异常
 * PreAuthenticatedCredentialsNotFoundException:身份预认证失败异常
 * ProviderNotFoundException:未配置AuthenticationProvider异常
 * AuthenticationServiceException:由于系统问题而无法处理认证请求异常
 * InternalAuthenticationServiceException:由于系统问题而无法处理认证请求异常，和AuthenticationServiceException不同之处在于如果外部系统出错，不会抛出该异常
 * AuthenticationCredentialsNotFoundException:SecuityContext 中不存在认证主体时抛出的异常
 * NonceExpiredException:HTTP摘要认证时随机数过期异常
 * RememberMeAuthenticationException:RememberMe认证异常
 * CookieTheftException :RememberMe认证时Cookie被盗窃异常
 * InvalidCookieException:RememberMe认证时无效的Cookie异常
 * AccountStatusException:账户状态异常
 * LockedException:账户被锁定异常
 * DisabledException:账户被禁用异常
 * CredentialsExpiredException:登录凭证（密码）过期异常
 * AccountExpiredException:账户过期异常
 * <p>
 * 1.2、AccessDeniedException(权限异常)
 * AccessDeniedException ：权限异常的父类
 * AuthorizationServiceException: 由于系统问题而无法处理权限时抛出异常
 * CsrfException:Csrf令牌异常
 * MissingCsrfTokenException:Csrf令牌缺失异常
 * InvalidCsrfTokenException:Csrf令牌无效异常
 */
@Slf4j
public class CairoAuthenticationFailedHandler extends AbstractHttpMessageHandler implements AuthenticationFailureHandler {

	@Getter
	@Setter
	@Accessors(fluent = true)
	private String realmName;

	public CairoAuthenticationFailedHandler(List<HttpMessageConverter<?>> converters) {
		super(converters, null);
	}

	public CairoAuthenticationFailedHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
		super(converters, contentNegotiationManager);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		log.debug("authentication failed ", exception);
		Business business = CairoAuthExceptionMapper.businessOf(exception);
		HttpStatus httpStatus = CairoAuthExceptionMapper.statusOf(exception);
		// TODO SessionAuthenticationException 允许用户同时在线 异常
		write(httpStatus, business, exception, request, response);

	}

	protected void write(HttpStatus httpStatus, Business business, AuthenticationException exception, HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotAcceptableException, IOException {
		response.setStatus(httpStatus.value());
		String message = Optional.ofNullable(exception.getMessage()).orElse(business.getMessage());

		BusinessResult<?> returnValue = BusinessResult.builder()
			.code(business.getCode())
			.message(message)
			.build();
		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));

	}
}
