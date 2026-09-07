package io.github.lijiajia3515.cairo.auth.framework.security.web;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.ServiceBusiness;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.www.NonceExpiredException;

/**
 * AuthenticationException → 业务码/HTTP 状态映射（自 {@link CairoAuthenticationFailedHandler} 原方法体抽出，
 * 两处共用：BusinessResult 信封输出（父处理器）与 token 端点纯 RFC 输出（error=业务码扩展），
 * 修改映射必须只改本类，避免双表漂移。分支顺序保持原样（后命中覆盖先命中）。
 */
public final class CairoAuthExceptionMapper {

	private CairoAuthExceptionMapper() {
	}

	public static Business businessOf(AuthenticationException exception) {
		Business business = ServiceBusiness.ERROR;
		if (exception instanceof AccountStatusException) {
			if (exception instanceof AccountExpiredException) {
				business = CairoAuthBusiness.ACCOUNT_LOCKED;
			} else if (exception instanceof DisabledException) {
				business = CairoAuthBusiness.ACCOUNT_DISABLED;
			} else if (exception instanceof LockedException) {
				business = CairoAuthBusiness.ACCOUNT_LOCKED;
			} else if ((exception instanceof CredentialsExpiredException)) {
				business = CairoAuthBusiness.TOKEN_EXPIRED;
			}
		}
		// 认证缺失
		if (exception instanceof AuthenticationCredentialsNotFoundException) {
			business = CairoAuthBusiness.TOKEN_INVALID;
		}

		// 凭证不足
		if (exception instanceof InsufficientAuthenticationException) {
			business = CairoAuthBusiness.DENIED;
		}

		// session 伪造 或者 解析失败
		if (exception instanceof RememberMeAuthenticationException) {
			business = CairoAuthBusiness.TOKEN_INVALID;
		}

		// 凭证错误
		if (exception instanceof BadCredentialsException) {
			business = CairoAuthBusiness.PASSWORD_BAD;
		}

		// 验证码错误
		if (exception instanceof VerifyCodeBadCredentialsException) {
			business = CairoAuthBusiness.VERIFY_CODE_BAD;
		}

		// SNS 授权码错误
		if (exception instanceof SnsCodeFailedException) {
			business = CairoAuthBusiness.SNS_CODE_BAD;
		}

		// security userDetail service 用户名不存在
		if (exception instanceof UsernameNotFoundException) {
			business = CairoAuthBusiness.ACCOUNT_NOT_FOUND;
		}

		// custom security userDetail service 账号不存在
		if (exception instanceof AccountNotFoundException) {
			business = CairoAuthBusiness.ACCOUNT_NOT_FOUND;
		}

		// 企业应用级用户不存在
		if (exception instanceof TenantAppUserNotFoundException) {
			business = CairoAuthBusiness.TENANT_APP_USER_NOT_FOUND;
		}

		// 企业应用级用户禁用
		if (exception instanceof TenantAppUserDisabledException) {
			business = CairoAuthBusiness.TENANT_APP_USER_DISABLED;
		}

		// 应用不存在
		if (exception instanceof AppNotFoundException) {
			business = CairoAuthBusiness.APP_NOT_FOUND;
		}

		// 应用被禁用
		if (exception instanceof AppDisabledException) {
			business = CairoAuthBusiness.APP_DISABLED;
		}

		// 客户端不存在
		if (exception instanceof ClientNotFoundException) {
			business = CairoAuthBusiness.CLIENT_NOT_FOUND;
		}

		// 客户端被禁用
		if (exception instanceof ClientDisabledException) {
			business = CairoAuthBusiness.CLIENT_DISABLED;
		}

		// 终端不存在
		if (exception instanceof EndpointNotFoundException) {
			business = CairoAuthBusiness.ENDPOINT_NOT_FOUND;
		}

		// 终端被禁用
		if (exception instanceof EndpointDisabledException) {
			business = CairoAuthBusiness.ENDPOINT_DISABLED;
		}

		// 企业应用未申请
		if (exception instanceof TenantAppNotApplyException) {
			business = CairoAuthBusiness.TENANT_APP_NOT_APPLY;
		}

		// 企业应用被禁用
		if (exception instanceof TenantAppDisabledException) {
			business = CairoAuthBusiness.TENANT_APP_DISABLED;
		}

		// 企业终端未申请
		if (exception instanceof TenantEndpointNotApplyException) {
			business = CairoAuthBusiness.TENANT_ENDPOINT_NOT_APPLY;
		}

		// 企业终端被禁用
		if (exception instanceof TenantEndpointDisabledException) {
			business = CairoAuthBusiness.TENANT_ENDPOINT_DISABLED;
		}

		// security 前置验证 异常
		if (exception instanceof PreAuthenticatedCredentialsNotFoundException) {
			business = CairoAuthBusiness.TOKEN_INVALID;
		}

		// HTTP摘要认证时随机数过期异常
		if (exception instanceof NonceExpiredException) {
			business = CairoAuthBusiness.NONCE_EXPIRED;
		}

		if (exception instanceof ProviderNotFoundException) {
			business = CairoAuthBusiness.NOT_SUPPORTED;
		}

		// security - 服务不可用
		if (exception instanceof AuthenticationServiceException) {
			business = CairoAuthBusiness.ERROR;
		}

		return business;
	}

	public static HttpStatus statusOf(AuthenticationException exception) {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		if (exception instanceof AccountStatusException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof AuthenticationCredentialsNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof InsufficientAuthenticationException) {
			httpStatus = HttpStatus.FORBIDDEN;
		}
		if (exception instanceof RememberMeAuthenticationException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof BadCredentialsException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof VerifyCodeBadCredentialsException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof SnsCodeFailedException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof UsernameNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof AccountNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantAppUserNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantAppUserDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof AppNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof AppDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof ClientNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof ClientDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof EndpointNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof EndpointDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantAppNotApplyException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantAppDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantEndpointNotApplyException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof TenantEndpointDisabledException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof PreAuthenticatedCredentialsNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof NonceExpiredException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof ProviderNotFoundException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		if (exception instanceof AuthenticationServiceException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
		}
		return httpStatus;
	}
}
