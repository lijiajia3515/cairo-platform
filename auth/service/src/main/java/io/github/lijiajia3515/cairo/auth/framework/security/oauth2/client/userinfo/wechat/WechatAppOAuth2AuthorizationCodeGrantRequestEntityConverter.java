package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.wechat;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class WechatAppOAuth2AuthorizationCodeGrantRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

	@Override
	public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
		HttpHeaders headers = HttpHeaders.EMPTY;
		MultiValueMap<String, String> formParameters = this.buildFormParameters(authorizationCodeGrantRequest);
		URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri()).build()
			.toUri();
		return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
	}

	private MultiValueMap<String, String> buildFormParameters(
		OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
		OAuth2AuthorizationExchange authorizationExchange = authorizationCodeGrantRequest.getAuthorizationExchange();
		MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
		formParameters.add(OAuth2ParameterNames.GRANT_TYPE, authorizationCodeGrantRequest.getGrantType().getValue());
		formParameters.add(OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());
		String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();
		String codeVerifier = authorizationExchange.getAuthorizationRequest().getAttribute(PkceParameterNames.CODE_VERIFIER);
		if (redirectUri != null) {
			formParameters.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
		}

		formParameters.add("appid", clientRegistration.getClientId());
		formParameters.add("secret", clientRegistration.getClientSecret());

		if (codeVerifier != null) {
			formParameters.add(PkceParameterNames.CODE_VERIFIER, codeVerifier);
		}
		return formParameters;
	}
}
