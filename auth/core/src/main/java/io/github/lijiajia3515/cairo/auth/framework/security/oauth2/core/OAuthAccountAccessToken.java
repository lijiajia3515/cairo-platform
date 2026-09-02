package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
public class OAuthAccountAccessToken extends OAuth2AccessToken {
    private final String accountId;

    private final String tokenId;

    private final Map<String, Object> claims;

    public OAuthAccountAccessToken(TokenType tokenType, String accountId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenType, tokenValue, issuedAt, expiresAt);
        this.accountId = accountId;
        this.tokenId = tokenId;
        this.claims = Collections.emptyMap();
    }

    public OAuthAccountAccessToken(TokenType tokenType, String accountId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes) {
        super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
        this.accountId = accountId;
        this.tokenId = tokenId;
        this.claims = Collections.emptyMap();
    }

    public OAuthAccountAccessToken(TokenType tokenType, String accountId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes, Map<String,Object> claims) {
        super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
        this.accountId = accountId;
        this.tokenId = tokenId;
        this.claims = claims;
    }
}
