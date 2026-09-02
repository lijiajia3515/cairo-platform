package io.github.lijiajia3515.cairo.redis;

import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * cairo Key 序列化
 */
public class CairoKeyRedisSerializer extends StringRedisSerializer {

	/**
	 * key前缀
	 */
	private final String keyPrefix;

	public CairoKeyRedisSerializer(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}


	@Override
	public String deserialize(byte[] bytes) {
		String value = super.deserialize(bytes);
		if (keyPrefix != null && value != null) {
			return keyPrefix.concat(value);
		}
		return value;
	}

	@Override
	public byte[] serialize(String string) {
		if (keyPrefix != null) {
			return super.serialize(keyPrefix.concat(string));
		}
		return super.serialize(string);
	}
}
