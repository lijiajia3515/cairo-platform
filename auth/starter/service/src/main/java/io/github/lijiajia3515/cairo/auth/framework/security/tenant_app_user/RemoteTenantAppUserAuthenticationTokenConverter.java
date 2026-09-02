package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationClientApiService;
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

public class RemoteTenantAppUserAuthenticationTokenConverter implements TenantAppUserAuthenticationTokenConverter {
	private final TenantAppUserAuthorizationClientApiService tenantAppUserAuthorizationClientApiService;

	public RemoteTenantAppUserAuthenticationTokenConverter(TenantAppUserAuthorizationClientApiService tenantAppUserAuthorizationClientApiService) {
		this.tenantAppUserAuthorizationClientApiService = tenantAppUserAuthorizationClientApiService;
	}

	@Override
	public Authentication convert(TenantAppUserAuthenticationTokenRequest tokenRequest) {
		try {
			TenantAppUserAuthorizationModel model = tenantAppUserAuthorizationClientApiService.getTenantAppUserAuthorization(
				GetTenantAppUserAuthorizationArgs.builder()
					.tenantId(tokenRequest.getTenantId())
					.appId(tokenRequest.getAppId())
					.accessToken(tokenRequest.getToken()).build()
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

				// 账号异常
				else if (CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode().equals(status)) {
					throw new AccountNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.ACCOUNT_LOCKED.getCode().equals(status)) {
					throw new LockedException(errorMessage);
				} else if (CairoAuthBusiness.ACCOUNT_DISABLED.getCode().equals(status)) {
					throw new DisabledException(errorMessage);
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

				// 企业异常
				else if (CairoAuthBusiness.TENANT_NOT_FOUND.getCode().equals(status)) {
					throw new TenantNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.TENANT_DISABLED.getCode().equals(status)) {
					throw new TenantDisabledException(errorMessage);
				}

				// 企业应用异常
				else if (CairoAuthBusiness.TENANT_APP_NOT_APPLY.getCode().equals(status)) {
					throw new TenantAppNotApplyException(errorMessage);
				} else if (CairoAuthBusiness.TENANT_APP_DISABLED.getCode().equals(status)) {
					throw new TenantAppDisabledException(errorMessage);
				}

				// 企业终端异常
				else if (CairoAuthBusiness.TENANT_ENDPOINT_NOT_APPLY.getCode().equals(status)) {
					throw new TenantEndpointNotApplyException(errorMessage);
				} else if (CairoAuthBusiness.TENANT_ENDPOINT_DISABLED.getCode().equals(status)) {
					throw new TenantEndpointDisabledException(errorMessage);
				}

				// 企业应用用户异常
				else if (CairoAuthBusiness.TENANT_APP_USER_NOT_FOUND.getCode().equals(status)) {
					throw new TenantAppUserNotFoundException(errorMessage);
				} else if (CairoAuthBusiness.TENANT_APP_USER_DISABLED.getCode().equals(status)) {
					throw new TenantAppUserDisabledException(errorMessage);
				} else {
					throw new AuthenticationServiceException("认证出错");
				}
			}

			OAuthTenantAppUserAccessToken accessToken = new OAuthTenantAppUserAccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				tokenRequest.getTenantId(),
				tokenRequest.getAppId(),
				tokenRequest.getEndpointId(),
				model.getUserId(),
				model.getTokenId(),
				tokenRequest.getToken(),
				model.getIssuedAt(),
				model.getExpiresAt(),
				Optional.ofNullable(model.getAuthorizedScopes()).orElse(Collections.emptySet())
			);


			CairoOAuthTenantAppUserPrincipal principal = CairoOAuthTenantAppUserPrincipal.builder()
				// 授权字段
				.id(model.getTokenId())
				.loginType(new LoginType(model.getLoginType()))
				.snsType(model.getSnsType())
				.tenantId(tokenRequest.getTenantId())
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
				.position(model.getPosition())
				.tags(model.getTags())
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

			return new CairoOAuthTenantAppUserAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
