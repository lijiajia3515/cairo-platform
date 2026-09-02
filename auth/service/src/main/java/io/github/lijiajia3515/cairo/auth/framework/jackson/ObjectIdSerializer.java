package io.github.lijiajia3515.cairo.auth.framework.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;

public class ObjectIdSerializer extends StdSerializer<ObjectId> {
	public ObjectIdSerializer() {
		super(ObjectId.class);
	}

	@Override
	public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(value.toHexString());
	}
}
