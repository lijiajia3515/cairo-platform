package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_REFRESH_TOKEN;

/**
 * 账号刷新令牌模式 authentication token
 */
@Getter
public class OAuthAccountRefreshTokenAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String refreshToken;
    private final Set<String> scopes;

    /**
     * Constructs an {@code OAuthAccountRefreshTokenAuthenticationToken} using the provided parameters.
     *
     * @param refreshToken  the account refresh token
     * @param clientPrincipal      the authenticated client principal
     * @param scopes               the requested scope(s)
     * @param additionalParameters the additional parameters
     */
    public OAuthAccountRefreshTokenAuthenticationToken(String refreshToken, Authentication clientPrincipal, @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {
        super(ACCOUNT_REFRESH_TOKEN, clientPrincipal, additionalParameters);
        Assert.hasText(refreshToken, "refreshToken cannot be empty");
        this.refreshToken = refreshToken;
        this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }
}
