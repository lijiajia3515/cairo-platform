package io.github.lijiajia3515.cairo.auth.framework.security.account;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.account.AccountAuthorizationClientApiService;
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

public class RemoteAccountAuthenticationTokenConverter implements AccountAuthenticationTokenConverter {
	private final AccountAuthorizationClientApiService accountClientApiService;

	public RemoteAccountAuthenticationTokenConverter(AccountAuthorizationClientApiService accountClientApiService) {
		this.accountClientApiService = accountClientApiService;
	}

	@Override
	public Authentication convert(String introspectedToken) {
		try {
			AccountAuthorizationModel model = accountClientApiService.getAccountAuthorization(
				GetAccountAuthorizationArgs.builder().accountAccessToken(introspectedToken).build()
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

			OAuthAccountAccessToken accessToken = new OAuthAccountAccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				model.getAccountId(),
				model.getTokenId(),
				introspectedToken,
				model.getIssuedAt(),
				model.getExpiresAt(),
				Optional.ofNullable(model.getAuthorizedScopes()).orElse(Collections.emptySet())
			);


			CairoOAuthAccountPrincipal principal = CairoOAuthAccountPrincipal.builder()
				.id(model.getTokenId())
				.loginType(new LoginType(model.getLoginType()))
				.snsType(model.getSnsType())
				.appId(model.getAppId())
				.clientId(model.getClientId())
				.accountId(model.getAccountId())
				.nickname(model.getNickname())
				.username(model.getUsername())
				.phoneNumber(model.getPhoneNumber())
				.email(model.getEmail())
				.avatarUrl(model.getAvatarUrl())
				.roles(Collections.emptyList())
				.departments(Collections.emptyList())
				.tags(Collections.emptyList())
				.locked(model.getLocked())
				.enabled(model.getEnabled())
				.build();
			List<SimpleGrantedAuthority> authorities = model.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

			return new CairoOAuthAccountAuthenticationToken(accessToken, principal, authorities);
		} catch (AuthenticationException e) {
			throw e;
		} catch (RuntimeException ex) {
			throw new AuthenticationServiceException("认证服务故障", ex);
		}
	}

}
