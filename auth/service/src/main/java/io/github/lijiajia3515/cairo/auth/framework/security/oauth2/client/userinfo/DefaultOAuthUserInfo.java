package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultOAuthUserInfo extends HashMap<String, Object> implements OAuthUserinfo, Serializable {
	private final String subjectKey;

	public DefaultOAuthUserInfo(String subjectKey, Map<? extends String, ?> m) {
		super(m);
		this.subjectKey = subjectKey;
	}

	@Override
	public String subject() {
		return Optional.ofNullable(get(subjectKey)).map(Object::toString).orElse(null);
	}
}
