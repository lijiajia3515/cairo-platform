package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.http;

import com.nimbusds.oauth2.sdk.token.BearerTokenError;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuth2Error;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class CairoOAuth2ResponseErrorHandler implements ResponseErrorHandler {

	private final HttpMessageConverter<CairoOAuth2Error> oauth2ErrorConverter;

	private final ResponseErrorHandler defaultErrorHandler = new DefaultResponseErrorHandler();

	public CairoOAuth2ResponseErrorHandler() {
		this.oauth2ErrorConverter = new CairoOAuth2ErrorHttpMessageConverter();
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return this.defaultErrorHandler.hasError(response);
	}

	@Override
	public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		if (HttpStatus.BAD_REQUEST.value() != response.getStatusCode().value()) {
			this.defaultErrorHandler.handleError(url, method, response);
		}
		try {
			CairoOAuth2Error cairoOAuth2Error = this.oauth2ErrorConverter.read(CairoOAuth2Error.class, response);
			// oauth2异常
			if (CairoOAuthBusiness.OAUTH_ERROR.code().equals(cairoOAuth2Error.getCode())) {
				OAuth2Error data = cairoOAuth2Error.getData();
				throw new OAuth2AuthorizationException(data);
			}
			// 过程异常
			else if (CairoAuthBusiness.PASSWORD_BAD.code().equals(cairoOAuth2Error.getCode())) {
				throw new BadCredentialsException("密码错误");
			} else if (CairoAuthBusiness.VERIFY_CODE_BAD.code().equals(cairoOAuth2Error.getCode())) {
				throw new VerifyCodeBadCredentialsException("验证码错误");
			}

			// 账号异常
			else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new AccountNotFoundException();
			} else if (CairoAuthBusiness.ACCOUNT_LOCKED.code().equals(cairoOAuth2Error.getCode())) {
				throw new LockedException("账号已锁定");
			} else if (CairoAuthBusiness.ACCOUNT_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new DisabledException("账号被禁用");
			}
			// 应用异常
			else if (CairoAuthBusiness.APP_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new AppNotFoundException();
			} else if (CairoAuthBusiness.APP_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new AppDisabledException();
			}
			// 客户端异常
			else if (CairoAuthBusiness.CLIENT_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new ClientNotFoundException();
			} else if (CairoAuthBusiness.CLIENT_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new ClientNotFoundException();
			}
			// 应用用户异常
			else if (CairoAuthBusiness.APP_USER_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new AppUserNotFoundException();
			} else if (CairoAuthBusiness.APP_USER_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new AppUserDisabledException();
			}
			// 租户异常
			else if (CairoAuthBusiness.TENANT_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantNotFoundException();
			} else if (CairoAuthBusiness.TENANT_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantDisabledException();
			} else if (CairoAuthBusiness.TENANT_APP_NOT_APPLY.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppNotApplyException();
			} else if (CairoAuthBusiness.TENANT_APP_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppDisabledException();
			}else if (CairoAuthBusiness.TENANT_ENDPOINT_NOT_APPLY.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppNotApplyException();
			} else if (CairoAuthBusiness.TENANT_ENDPOINT_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppDisabledException();
			}
			// 企业用户异常
			else if (CairoAuthBusiness.TENANT_APP_USER_NOT_FOUND.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppUserNotFoundException();
			} else if (CairoAuthBusiness.TENANT_APP_USER_DISABLED.code().equals(cairoOAuth2Error.getCode())) {
				throw new TenantAppUserDisabledException();
			}
		} catch (OAuth2AuthorizationException e) {
			throw e;
		} catch (Exception e) {
			throw new OAuth2AuthorizationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), e);
		}
	}


	private OAuth2Error readErrorFromWwwAuthenticate(HttpHeaders headers) {
		String wwwAuthenticateHeader = headers.getFirst(HttpHeaders.WWW_AUTHENTICATE);
		if (!StringUtils.hasText(wwwAuthenticateHeader)) {
			return null;
		}
		BearerTokenError bearerTokenError = getBearerToken(wwwAuthenticateHeader);
		if (bearerTokenError == null) {
			return new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, null, null);
		}
		String errorCode = (bearerTokenError.getCode() != null) ? bearerTokenError.getCode() : OAuth2ErrorCodes.SERVER_ERROR;
		String errorDescription = bearerTokenError.getDescription();
		String errorUri = (bearerTokenError.getURI() != null) ? bearerTokenError.getURI().toString() : null;
		return new OAuth2Error(errorCode, errorDescription, errorUri);
	}

	private BearerTokenError getBearerToken(String wwwAuthenticateHeader) {
		try {
			return BearerTokenError.parse(wwwAuthenticateHeader);
		} catch (Exception ex) {
			return null;
		}
	}
}
