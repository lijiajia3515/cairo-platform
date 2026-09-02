package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import java.util.Map;

public class DefaultOAuth2UserInfoResponseClient implements OAuthUserInfoResponseClient<DefaultOAuthUserInfo> {
	protected final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";

	protected RestOperations restOperations;

	public DefaultOAuth2UserInfoResponseClient() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		this.restOperations = restTemplate;
	}

	public DefaultOAuthUserInfo getResponse(OAuth2UserRequest userRequest, RequestEntity<?> request) {
		try {
			return new DefaultOAuthUserInfo(
				userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(),
				this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE).getBody()
			);
		} catch (OAuth2AuthorizationException ex) {
			OAuth2Error oauth2Error = ex.getError();
			StringBuilder errorDetails = new StringBuilder();
			errorDetails.append("Error details: [");
			errorDetails.append("UserInfo Uri: ")
				.append(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
			errorDetails.append(", Error Code: ").append(oauth2Error.getErrorCode());
			if (oauth2Error.getDescription() != null) {
				errorDetails.append(", Error Description: ").append(oauth2Error.getDescription());
			}
			errorDetails.append("]");
			oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
				"An error occurred while attempting to retrieve the UserInfo Resource: " + errorDetails.toString(),
				null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		} catch (UnknownContentTypeException ex) {
			String errorMessage = "An error occurred while attempting to retrieve the UserInfo Resource from '"
				+ userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
				+ "': response contains invalid content type '" + ex.getContentType().toString() + "'. "
				+ "The UserInfo Response should return a JSON object (content type 'application/json') "
				+ "that contains a collection of name and value pairs of the claims about the authenticated End-User. "
				+ "Please ensure the UserInfo Uri in UserInfoEndpoint for Client Registration '"
				+ userRequest.getClientRegistration().getRegistrationId() + "' conforms to the UserInfo Endpoint, "
				+ "as defined in OpenID Connect 1.0: 'https://openid.net/specs/openid-connect-core-1_0.html#UserInfo'";
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		} catch (RestClientException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
				"An error occurred while attempting to retrieve the UserInfo Resource: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
	}

	/**
	 * Sets the {@link RestOperations} used when requesting the UserInfo resource.
	 *
	 * <p>
	 * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be configured
	 * with the following:
	 * <ol>
	 * <li>{@link ResponseErrorHandler} - {@link OAuth2ErrorResponseErrorHandler}</li>
	 * </ol>
	 *
	 * @param restOperations the {@link RestOperations} used when requesting the UserInfo
	 *                       resource
	 * @since 5.1
	 */
	public final void setRestOperations(RestOperations restOperations) {
		Assert.notNull(restOperations, "restOperations cannot be null");
		this.restOperations = restOperations;
	}

}
