package io.github.lijiajia3515.cairo.auth.framework.security.subapp_user;

import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization.SubappUserAuthorizationClientApiService;
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

public class RemoteSubappUserAuthenticationTokenConverter implements SubappUserAuthenticationTokenConverter {
	private final SubappUserAuthorizationClientApiService subappUserAuthorizationClientApiService;

	public RemoteSubappUserAuthenticationTokenConverter(SubappUserAuthorizationClientApiService subappUserAuthorizationClientApiService) {
		this.subappUserAuthorizationClientApiService = subappUserAuthorizationClientApiService;
	}

	@Override
	public Authentication convert(SubappUserAuthenticationTokenRequest tokenRequest) {
		try {
			SubappUserAuthorizationModel model = subappUserAuthorizationClientApiService.getSubappUserAuthorization(
				GetSubappUserAuthorizationArgs.builder()
					.appId(tokenRequest.getAppId())
					.endpointId(tokenRequest.getEndpointId())
					.accessToken(tokenRequest.getToken())
					.subappId(tokenRequest.getSubappId())
					.subappVersion(tokenRequest.getSubappVersion())
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

				// 应用异常
				else if (CairoAuthBusiness.APP_NOT_FOUND.getCode().equals(status)) {
					throw new AppNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.APP_DISABLED.getCode().equals(status)) {
					throw new AppDisabledException(errorMessage);
				}

				// 终端异常
				if (CairoAuthBusiness.ENDPOINT_NOT_FOUND.getCode().equals(status)) {
					throw new EndpointNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new EndpointDisabledException(errorMessage);
				}

				// 子应用异常
				else if (CairoAuthBusiness.SUBAPP_NOT_FOUND.getCode().equals(status)) {
					throw new SubappNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.SUBAPP_DISABLED.getCode().equals(status)) {
					throw new SubappDisabledException(errorMessage);
				}

				// 用户异常
				else if (CairoAuthBusiness.APP_USER_NOT_FOUND.getCode().equals(status)) {
					throw new AppUserNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.APP_USER_DISABLED.getCode().equals(status)) {
					throw new AppUserDisabledException(errorMessage);
				}

				// 子应用权限异常
				else if (CairoAuthBusiness.SUBAPP_NOT_APPLY.getCode().equals(status)) {
					throw new SubappNotApplyException(errorMessage);
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


			CairoOAuthSubappUserPrincipal principal = CairoOAuthSubappUserPrincipal.builder()
				// 授权字段
				.id(model.getTokenId())
				.appId(tokenRequest.getAppId())
				.endpointId(tokenRequest.getEndpointId())
				.subappId(tokenRequest.getSubappId())
				.subappVersion(tokenRequest.getSubappVersion())
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

			return new CairoOAuthSubappUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
