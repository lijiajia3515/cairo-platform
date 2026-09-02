package io.github.lijiajia3515.cairo.auth.framework.idempotent;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class RedisIdempotentServiceImpl implements IdempotentService {
	private final RedisTemplate<String, Object> redisTemplate;


	public static final String REDIS_KEY = "idempotent_token:";

	public RedisIdempotentServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override

	public boolean check(String token, Duration timeout) {
		if (token == null) {
			throw new BadTokenIdempotentException("token 不能为空");
		}
		String key = REDIS_KEY + token;
		Long increment = redisTemplate.opsForValue().increment(key, 1);
		redisTemplate.expire(key, timeout);
		return increment != null && increment <= 1L;
	}
}
