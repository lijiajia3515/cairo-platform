package io.github.lijiajia3515.cairo.auth.framework.sdk;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

public class CairoOAuthClientSdkService {
	private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
	private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
	private final String clientRegistrationId;

	public CairoOAuthClientSdkService(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager, String clientRegistrationId) {
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
		this.clientRegistrationId = clientRegistrationId;
	}

	public String getHeaderAuthorization() {
		OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
			.withClientRegistrationId(clientRegistrationId)
			.principal(ANONYMOUS_AUTHENTICATION)
			.build();

		try {
			OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientManager.authorize(request);
			if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
				OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
				return String.format("%s %s", accessToken.getTokenType().getValue(), accessToken.getTokenValue());
			}

		} catch (OAuth2AuthorizationException ex) {
			throw new RuntimeException("cannot get accessToken", ex);
		}
		return null;
	}
}
