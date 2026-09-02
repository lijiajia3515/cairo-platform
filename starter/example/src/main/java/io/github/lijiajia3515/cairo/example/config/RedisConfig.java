//package io.github.lijiajia3515.cairo.example.config;
//
//
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RedisConfig implements ApplicationRunner {
//
//	private final RedisTemplate<String, Object> redisTemplate;
//
//	public RedisConfig(RedisTemplate<String, Object> redisTemplate) {
//		this.redisTemplate = redisTemplate;
//	}
//
//	@Override
//	public void run(ApplicationArguments args) throws Exception {
//		redisTemplate.opsForValue().set("a", "a");
//		Object a = redisTemplate.opsForValue().get("a");
//		System.out.println(a);
//	}
//}
