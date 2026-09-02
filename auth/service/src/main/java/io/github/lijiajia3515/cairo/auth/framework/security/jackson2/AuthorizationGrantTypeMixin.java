package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationGrantTypeMixin {

	@JsonCreator
    AuthorizationGrantTypeMixin(@JsonProperty("value") String value) {
	}
}
