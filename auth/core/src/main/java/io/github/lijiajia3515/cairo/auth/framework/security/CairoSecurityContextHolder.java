package io.github.lijiajia3515.cairo.auth.framework.security;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class CairoSecurityContextHolder {

	public static Optional<CairoOAuthClientPrincipal> getClient() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthClientPrincipal)) {
					return Optional.ofNullable((CairoOAuthClientPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}

	public static Optional<CairoOAuthAccountPrincipal> getAccount() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthAccountPrincipal)) {
					return Optional.ofNullable((CairoOAuthAccountPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}


	public static Optional<CairoOAuthAppUserPrincipal> getAppUser() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthAppUserPrincipal)) {
					return Optional.ofNullable((CairoOAuthAppUserPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}

	public static Optional<CairoOAuthSubappUserPrincipal> getSubappUser() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthSubappUserPrincipal)) {
					return Optional.ofNullable((CairoOAuthSubappUserPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}

	public static Optional<CairoOAuthTenantAppUserPrincipal> getTenantAppUser() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthTenantAppUserPrincipal)) {
					return Optional.ofNullable((CairoOAuthTenantAppUserPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}

	public static Optional<CairoOAuthTenantSubappUserPrincipal> getTenantSubappUser() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if ((authentication.getPrincipal() instanceof CairoOAuthTenantSubappUserPrincipal)) {
					return Optional.ofNullable((CairoOAuthTenantSubappUserPrincipal) authentication.getPrincipal());
				}
				return Optional.empty();
			});
	}

	public static String getAccountId() {
		return CairoSecurityContextHolder.getAccount().map(CairoOAuthAccountPrincipal::getAccountId)
			.or(() -> CairoSecurityContextHolder.getAppUser().map(CairoOAuthAppUserPrincipal::getAccountId))
			.or(() -> CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getAccountId))
			.or(() -> CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getAccountId))
			.or(() -> CairoSecurityContextHolder.getTenantSubappUser().map(CairoOAuthTenantSubappUserPrincipal::getAccountId))
			.orElse(null);
	}

	public static String getClientId() {
		return CairoSecurityContextHolder.getClient().map(CairoOAuthClientPrincipal::getClientId)
			.or(() -> CairoSecurityContextHolder.getAccount().map(CairoOAuthAccountPrincipal::getClientId))
			.or(() -> CairoSecurityContextHolder.getAppUser().map(CairoOAuthAppUserPrincipal::getClientId))
			.or(() -> CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getClientId))
			.orElse(null);
	}

	public static String getAppId() {
		return CairoSecurityContextHolder.getClient().map(CairoOAuthClientPrincipal::getAppId)
			.or(() -> CairoSecurityContextHolder.getAccount().map(CairoOAuthAccountPrincipal::getAppId))
			.or(() -> CairoSecurityContextHolder.getAppUser().map(CairoOAuthAppUserPrincipal::getAppId))
			.or(() -> CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getAppId))
			.or(() -> CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getAppId))
			.or(() -> CairoSecurityContextHolder.getTenantSubappUser().map(CairoOAuthTenantSubappUserPrincipal::getAppId))
			.orElse(null);
	}

	public static String getEndpointId() {
		return CairoSecurityContextHolder.getAppUser().map(CairoOAuthAppUserPrincipal::getEndpointId)
			.or(() -> CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getEndpointId))
			.or(() -> CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getEndpointId))
			.or(() -> CairoSecurityContextHolder.getTenantSubappUser().map(CairoOAuthTenantSubappUserPrincipal::getEndpointId))
			.orElse(null);
	}

	public static String getAppUserId() {
		return CairoSecurityContextHolder.getAppUser().map(CairoOAuthAppUserPrincipal::getUserId)
			.or(() -> CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getUserId))
			.orElse(null);
	}

	public static String getSubappUserId() {
		return CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getUserId)
			.orElse(null);
	}

	public static String getSubappAccountId() {
		return CairoSecurityContextHolder.getSubappUser().map(CairoOAuthSubappUserPrincipal::getAccountId)
			.orElse(null);
	}

	public static String getTenantId() {
		return CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getTenantId)
			.or(() -> CairoSecurityContextHolder.getTenantSubappUser().map(CairoOAuthTenantSubappUserPrincipal::getTenantId))
			.orElse(null);
	}

	public static String getTenantAppUserId() {
		return CairoSecurityContextHolder.getTenantAppUser().map(CairoOAuthTenantAppUserPrincipal::getUserId)
			.or(() -> CairoSecurityContextHolder.getTenantSubappUser().map(CairoOAuthTenantSubappUserPrincipal::getUserId))
			.orElse(null);
	}


}
