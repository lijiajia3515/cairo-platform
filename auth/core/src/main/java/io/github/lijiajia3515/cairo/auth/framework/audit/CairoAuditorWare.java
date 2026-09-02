package io.github.lijiajia3515.cairo.auth.framework.audit;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 获取身份标识
 */
public class CairoAuditorWare implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {

		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.flatMap(authentication -> {
				if (authentication instanceof CairoOAuthClientAuthenticationToken) {
					return Optional.ofNullable(((CairoOAuthClientAuthenticationToken) authentication).getPrincipal())
						.map(CairoOAuthClientPrincipal::getClientId);
				}

				if (authentication instanceof CairoOAuthAccountAuthenticationToken) {
					return Optional.ofNullable(((CairoOAuthAccountAuthenticationToken) authentication).getPrincipal())
						.map(CairoOAuthAccountPrincipal::getAccountId);
				}

				if (authentication instanceof CairoOAuthAppUserAuthenticationToken) {
					return Optional.ofNullable(((CairoOAuthAppUserAuthenticationToken) authentication).getPrincipal())
						.map(CairoOAuthAppUserPrincipal::getUserId);
				}

				if (authentication instanceof CairoOAuthTenantAppUserAuthenticationToken) {
					return Optional.ofNullable(((CairoOAuthTenantAppUserAuthenticationToken) authentication).getPrincipal())
							.map(CairoOAuthTenantAppUserPrincipal::getUserId);
				}

				return Optional.empty();
			});
	}
}
