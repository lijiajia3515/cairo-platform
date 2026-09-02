package io.github.lijiajia3515.cairo.gateway.framework.redis;

import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CairoRedisKeySerializer extends StringRedisSerializer {
	private final CairoRedisProperties properties;
	static final Charset CHARSET = StandardCharsets.UTF_8;

	public CairoRedisKeySerializer(CairoRedisProperties properties) {
		super(CHARSET);
		this.properties = properties;
	}

	@Override
	public String deserialize(byte[] bytes) {
		return (bytes == null ? null : properties.getKeyPrefix().concat(new String(bytes, CHARSET)));
	}

	@Override
	public byte[] serialize(String string) {
		return (string == null ? null : properties.getKeyPrefix().concat(string).getBytes(CHARSET));
	}
}
