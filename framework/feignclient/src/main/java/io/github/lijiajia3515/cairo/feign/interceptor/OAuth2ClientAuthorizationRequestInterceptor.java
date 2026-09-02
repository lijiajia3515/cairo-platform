package io.github.lijiajia3515.cairo.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

/**
 * 基于 oauth2 client manager 实现 feign认证实现
 */
@Slf4j
public class OAuth2ClientAuthorizationRequestInterceptor implements RequestInterceptor {
	private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous",
		"anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
	private final AuthorizedClientServiceOAuth2AuthorizedClientManager manager;
	private final String registrationId;


	public OAuth2ClientAuthorizationRequestInterceptor(AuthorizedClientServiceOAuth2AuthorizedClientManager manager, @Qualifier("defaultClientRegistrationId") String defaultRegistrationId) {
		this.manager = manager;
		this.registrationId = defaultRegistrationId;
	}

	@Override
	public void apply(RequestTemplate template) {

		OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
			.withClientRegistrationId(registrationId)
			.principal(ANONYMOUS_AUTHENTICATION)
			.build();

		try {
			OAuth2AuthorizedClient authorizedClient = manager.authorize(request);
			if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
				OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
				template.header(HttpHeaders.AUTHORIZATION, String.format("%s %s", accessToken.getTokenType().getValue(), accessToken.getTokenValue()));
			}

		} catch (OAuth2AuthorizationException ex) {
			throw new RuntimeException("cannot get accessToken", ex);
		}
	}
}
