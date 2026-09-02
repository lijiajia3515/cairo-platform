package io.github.lijiajia3515.cairo.auth.framework.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Optional;

public class AuthorizationGrantTypeRedisSerializer implements RedisSerializer<AuthorizationGrantType> {
	@Override
	public byte[] serialize(AuthorizationGrantType authorizationGrantType) throws SerializationException {
		return Optional.ofNullable(authorizationGrantType).map(AuthorizationGrantType::getValue).map(String::getBytes).orElse(new byte[0]);
	}

	@Override
	public AuthorizationGrantType deserialize(byte[] bytes) throws SerializationException {
		return new AuthorizationGrantType(new String(bytes));
	}
}
