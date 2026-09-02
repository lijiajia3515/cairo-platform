package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
public class OAuthAppUserAccessToken extends OAuth2AccessToken {

	private final String appId;

	private final String endpointId;

	private final String userId;

	private final String tokenId;

	private final Map<String, Object> claims;

	public OAuthAppUserAccessToken(TokenType tokenType, String appId, String endpointId, String userId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt) {
		super(tokenType, tokenValue, issuedAt, expiresAt);
		this.appId = appId;
		this.endpointId = endpointId;
		this.userId = userId;
		this.tokenId = tokenId;
		this.claims = Collections.emptyMap();
	}

	public OAuthAppUserAccessToken(TokenType tokenType, String appId, String endpointId, String userId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes) {
		super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
		this.appId = appId;
		this.endpointId = endpointId;
		this.userId = userId;
		this.tokenId = tokenId;
		this.claims = Collections.emptyMap();
	}

	public OAuthAppUserAccessToken(TokenType tokenType, String appId, String endpointId, String userId, String tokenId, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes, Map<String, Object> claims) {
		super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
		this.appId = appId;
		this.endpointId = endpointId;
		this.userId = userId;
		this.tokenId = tokenId;
		this.claims = claims;
	}
}
