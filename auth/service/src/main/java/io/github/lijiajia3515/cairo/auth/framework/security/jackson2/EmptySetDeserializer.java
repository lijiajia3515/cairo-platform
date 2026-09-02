package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

final class EmptySetDeserializer extends JsonDeserializer<Set<?>> {

	@Override
	public Set<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		return Collections.emptySet();
	}

}
