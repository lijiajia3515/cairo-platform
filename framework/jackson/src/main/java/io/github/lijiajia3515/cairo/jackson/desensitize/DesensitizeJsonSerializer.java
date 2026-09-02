package io.github.lijiajia3515.cairo.jackson.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;

public class DesensitizeJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {
	private DesensitizeType type;

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		// 在序列化时进行数据脱敏
		gen.writeString(type.desensitize(value));
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		Desensitize annotation = property.getAnnotation(Desensitize.class);
		if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
			if (annotation.type() != null) {
				this.type = annotation.type();
				return this;
			}
		}
		return prov.findValueSerializer(property.getType(), property);
	}
}
