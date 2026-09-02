package io.github.lijiajia3515.cairo.auth.framework.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;

public class AuthorizationGrantTypeDeserializer extends StdDeserializer<AuthorizationGrantType> {

	private static final long serialVersionUID = 1L;

	public AuthorizationGrantTypeDeserializer() {
		super(AuthorizationGrantType.class);
	}

	@Override
	public AuthorizationGrantType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return new AuthorizationGrantType(_parseString(p, ctxt));
	}
}
