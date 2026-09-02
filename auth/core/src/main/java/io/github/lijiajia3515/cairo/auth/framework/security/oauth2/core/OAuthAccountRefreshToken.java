package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.time.Instant;

@Getter
public class OAuthAccountRefreshToken extends OAuth2RefreshToken {
	public OAuthAccountRefreshToken(String tokenValue, Instant issuedAt) {
		super(tokenValue, issuedAt);
	}

	public OAuthAccountRefreshToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
		super(tokenValue, issuedAt, expiresAt);
	}
}
