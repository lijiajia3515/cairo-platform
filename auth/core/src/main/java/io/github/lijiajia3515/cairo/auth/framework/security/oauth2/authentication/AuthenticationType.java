package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;

import lombok.Getter;
import org.springframework.util.Assert;

import java.io.Serializable;

@Getter
public final class AuthenticationType implements Serializable {


	public static final AuthenticationType CLIENT = new AuthenticationType("client");

	public static final AuthenticationType ACCOUNT = new AuthenticationType("account");

	public static final AuthenticationType APP_USER = new AuthenticationType("app_user");

	public static final AuthenticationType TENANT_APP_USER = new AuthenticationType("tenant_app_user");


	/**
	 * type value
	 */
	private final String value;

	public AuthenticationType(String value) {
		Assert.hasText(value, "value cannot be empty");
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		AuthenticationType that = (AuthenticationType) obj;
		return this.getValue().equalsIgnoreCase(that.getValue());
	}

	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}

}
