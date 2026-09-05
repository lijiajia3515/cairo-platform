package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.TENANT_APP_USER_REFRESH_TOKEN;

/**
 * 应用级用户刷新令牌模式 authentication token
 */
@Getter
public class OAuthTenantAppUserRefreshTokenAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
	private final String tenantId;
	private final String refreshToken;
	private final Set<String> scopes;

	/**
	 * Constructs an {@code OAuthAccountRefreshTokenAuthenticationToken} using the provided parameters.
	 *
	 * @param refreshToken         the endpoint user refresh token
	 * @param clientPrincipal      the authenticated client principal
	 * @param scopes               the requested scope(s)
	 * @param additionalParameters the additional parameters
	 */
	public OAuthTenantAppUserRefreshTokenAuthenticationToken(String tenantId, String refreshToken, Authentication clientPrincipal, @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {
		super(TENANT_APP_USER_REFRESH_TOKEN, clientPrincipal, additionalParameters);
		Assert.hasText(tenantId, "tenantId cannot be empty");
		Assert.hasText(refreshToken, "refreshToken cannot be empty");
		this.tenantId = tenantId;
		this.refreshToken = refreshToken;
		this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
	}
}
