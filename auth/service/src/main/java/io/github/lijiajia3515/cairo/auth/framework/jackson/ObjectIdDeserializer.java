package io.github.lijiajia3515.cairo.auth.framework.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bson.types.ObjectId;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;

public class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

	private static final long serialVersionUID = 1L;

	public ObjectIdDeserializer() {
		super(ObjectId.class);
	}

	@Override
	public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return new ObjectId(_parseString(p, ctxt));
	}
}
