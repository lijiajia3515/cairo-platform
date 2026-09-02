package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.github.GithubOAuthUserinfoResponseClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.wechat.WechatWebOAuth2UserinfoResponseClient;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.util.HashMap;
import java.util.Map;

public class CommonOAuth2UserInfoResponseClient implements OAuthUserInfoResponseClient<OAuthUserinfo> {
	private final OAuthUserInfoResponseClient<? extends OAuthUserinfo> DEFAULT = new DefaultOAuth2UserInfoResponseClient();
	private final Map<String, OAuthUserInfoResponseClient<? extends OAuthUserinfo>> clients = new HashMap<>();

	public CommonOAuth2UserInfoResponseClient() {
		clients.put("wechat-web", new WechatWebOAuth2UserinfoResponseClient());
		clients.put("wechat-open", new WechatWebOAuth2UserinfoResponseClient());
		clients.put("github", new GithubOAuthUserinfoResponseClient());
	}

	@Override
	public OAuthUserinfo getResponse(OAuth2UserRequest userRequest, RequestEntity<?> request) {
		return clients.getOrDefault(userRequest.getClientRegistration().getRegistrationId(), DEFAULT).getResponse(userRequest, request);
	}
}
