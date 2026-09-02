package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.AppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationClientApiService;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RemoteAppUserAuthenticationTokenConverter implements AppUserAuthenticationTokenConverter {
	private final AppUserAuthorizationClientApiService appUserAuthorizationClientApiService;

	public RemoteAppUserAuthenticationTokenConverter(AppUserAuthorizationClientApiService appUserAuthorizationClientApiService) {
		this.appUserAuthorizationClientApiService = appUserAuthorizationClientApiService;
	}

	@Override
	public Authentication convert(AppUserAuthenticationTokenRequest tokenRequest) {
		try {
			AppUserAuthorizationModel model = appUserAuthorizationClientApiService.getAppUserAuthorization(
				GetAppUserAuthorizationArgs.builder()
					.appId(tokenRequest.getAppId())
					.endpointId(tokenRequest.getEndpointId())
					.accessToken(tokenRequest.getToken())
					.build()
			);

			String status = model.getStatus();
			if (!DefaultBusiness.SUCCESS.getCode().equals(status)) {
				String errorMessage = model.getErrorMessage();
				// 授权异常
				if (CairoAuthBusiness.TOKEN_INVALID.getCode().equals(status)) {
					throw new TokenInvalidException(errorMessage);
				} else if (CairoAuthBusiness.TOKEN_EXPIRED.getCode().equals(status)) {
					throw new TokenInvalidException(errorMessage);
				}

				// 客户端异常
				else if (CairoAuthBusiness.CLIENT_NOT_FOUND.getCode().equals(status)) {
					throw new ClientNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.CLIENT_DISABLED.getCode().equals(status)) {
					throw new ClientDisabledException(errorMessage);
				}

				// 应用异常
				else if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException(errorMessage);
				}

				// 终端异常
				else if (CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode().equals(status)) {
					throw new EndpointNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new EndpointDisabledException(errorMessage);
				}

				// 用户异常
				else if (CairoAuthBusiness.APP_USER_NOT_FOUND.getCode().equals(status)) {
					throw new AppUserNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.APP_USER_DISABLED.getCode().equals(status)) {
					throw new AppUserDisabledException(errorMessage);
				}

				// 账号异常
				else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException(errorMessage);
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException(errorMessage);
				}
				// 兜底
				else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			OAuthAppUserAccessToken accessToken = new OAuthAppUserAccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				tokenRequest.getAppId(),
				tokenRequest.getEndpointId(),
				model.getUserId(),
				model.getTokenId(),
				tokenRequest.getToken(),
				model.getIssuedAt(),
				model.getExpiresAt(),
				Optional.ofNullable(model.getAuthorizedScopes()).orElse(Collections.emptySet())
			);


			CairoOAuthAppUserPrincipal principal = CairoOAuthAppUserPrincipal.builder()
				// 授权字段
				.id(model.getTokenId())
				.loginType(new LoginType(model.getLoginType()))
				.snsType(model.getSnsType())
				.appId(tokenRequest.getAppId())
				.endpointId(tokenRequest.getEndpointId())
				.clientId(model.getClientId())
				// 用户字段
				.userId(model.getUserId())
				.nickname(model.getNickname())
				.phoneNumber(model.getPhoneNumber())
				.userEnabled(model.isUserEnabled())
				.appAdmin(model.isAppAdmin())
				.roles(model.getRoles())
				.departments(model.getDepartments())
				.tags(model.getTags())
				.position(model.getPosition())
				// 账号字段
				.accountId(model.getAccountId())
				.accountUsername(model.getAccountUsername())
				.accountPhoneNumber(model.getAccountPhoneNumber())
				.accountEmail(model.getAccountEmail())
				.accountAvatarUrl(model.getAccountAvatarUrl())
				.accountEnabled(model.isAccountEnabled())
				.accountLocked(model.isAccountLocked())
				.build();
			List<SimpleGrantedAuthority> authorities = model.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

			return new CairoOAuthAppUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
