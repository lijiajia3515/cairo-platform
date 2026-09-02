package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2ErrorMixin {

	@JsonCreator
	OAuth2ErrorMixin(
		@JsonProperty("ErrorCode") String errorCode,
		@JsonProperty("Description") String description,
		@JsonProperty("Uri") String uri) {
	}
}
