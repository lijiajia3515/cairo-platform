package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token.TokenKeyGenerator;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;

@Component
public class RedisCaptchaTokenServiceImpl implements CaptchaTokenService {

	/**
	 * token 生成器
	 */
	private final StringKeyGenerator codeGenerator = new TokenKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32, "captcha_");


	/**
	 * 验证token 过期时间
	 */
	private static final Duration TOKEN_DURATION = Duration.ofMinutes(30);

	private static final String PREFIX = "captcha:token";
	private final RedisTemplate<String, Object> redisTemplate;

	public RedisCaptchaTokenServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public CaptchaToken storeToken(StoreCaptchaTokenArgs args) {
		// 生成验证码token
		String token = codeGenerator.generateKey();
		CaptchaToken captchaToken = CaptchaToken.builder()
			.token(token)
			.ip(args.getIp())
			.ttl(TOKEN_DURATION.toSeconds())
			.count(0)
			.build();
		String tokenKey = getTokenKey(token);
		redisTemplate.opsForValue().set(tokenKey, captchaToken, TOKEN_DURATION);
		return captchaToken;
	}

	@Override
	@NewSpan
	public boolean verifyToken(VerifyCaptchaTokenArgs args) {
		String redisTokenKey = getTokenKey(args.getToken());

		CaptchaToken storeToken = (CaptchaToken) redisTemplate.opsForValue().get(redisTokenKey);

		if (storeToken == null) {
			return false;
		}
		if (storeToken.getIp().equalsIgnoreCase(args.getIp())) {
			redisTemplate.delete(redisTokenKey);
			return true;
		} else {
			// 允许重试1次
			if (storeToken.getCount() > 1) {
				redisTemplate.delete(redisTokenKey);
				return false;
			}
			storeToken.setCount(storeToken.getCount() + 1);
			redisTemplate.opsForValue().set(redisTokenKey, storeToken,TOKEN_DURATION);
			return false;
		}
	}


	private String getTokenKey(String token) {
		return String.format("%s:%s", PREFIX, token);
	}
}
