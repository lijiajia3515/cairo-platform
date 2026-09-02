//package com.yr.cairo.starter.rabbit;
//
//import com.yr.cairo.auth.famework.security.CairoAuthenticationToken;
//import org.springframework.amqp.AmqpException;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessagePostProcessor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//
//import java.util.Map;
//
//public class AuthorizationMessagePostProcessor implements MessagePostProcessor {
//	private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous",
//		"anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
//	private final AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager;
//
//	public AuthorizationMessagePostProcessor(AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager) {
//		this.clientManager = clientManager;
//	}
//
//	@Override
//	public Message postProcessMessage(Message message) throws AmqpException {
//		Map<String, Object> headers = message.getMessageProperties().getHeaders();
//
//		Object authorization = headers.get(HttpHeaders.AUTHORIZATION);
//		if (authorization != null) {
//			return message;
//		}
//
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (authorization != null && authentication instanceof CairoAuthenticationToken) {
//			CairoAuthenticationToken cairoAuthenticationToken = (CairoAuthenticationToken) authentication;
//			if (cairoAuthenticationToken.getPrincipal() != null) {
//				headers.put("Tenant-Id", cairoAuthenticationToken.getPrincipal().getTenantId());
//				headers.put(HttpHeaders.AUTHORIZATION, String.format("%s %s", cairoAuthenticationToken.getPrincipal().getTokenType(), cairoAuthenticationToken.getPrincipal().getAccessToken()));
//			}
//		} else {
//			try {
//				OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("default-client")
//					.principal(ANONYMOUS_AUTHENTICATION)
//					.build();
//				OAuth2AuthorizedClient authorize = clientManager.authorize(request);
//				OAuth2AccessToken accessToken = authorize.getAccessToken();
//				headers.put(HttpHeaders.AUTHORIZATION, String.format("%s %s", accessToken.getTokenType().getValue(), accessToken.getTokenValue()));
//				headers.put("Tenant-Id", "default");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		return message;
//	}
//}
