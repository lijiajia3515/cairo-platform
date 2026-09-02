package io.github.lijiajia3515.cairo.auth.modules.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeModel;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyService;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.NewAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * AuthCode 存储和检验实现
 */
@Slf4j
@Component
public class RedisAuthCodeServiceImpl implements AuthCodeStoreService, AuthCodeVerifyService, Serializable {
	public static final String REDIS_KEY = "auth_code";
	/**
	 * token 生成器
	 */
	private final StringKeyGenerator TOKEN_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32);

	/**
	 * 验证token 过期时间
	 */
	private static final Duration TOKEN_DURATION = Duration.ofMinutes(30);


	public final RedisTemplate<String, Object> redisTemplate;

	public RedisAuthCodeServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public AuthCodeModel generate(NewAuthCodeArgs args) {
		String accountId = args.getAccountId();
		String token = TOKEN_GENERATOR.generateKey();

		LocalDateTime expireTime = LocalDateTime.now().plus(TOKEN_DURATION);

		AuthCodeModel authCodeModel = AuthCodeModel.builder()
			.authCode(token)
			.ip(args.getIp())
			.expireTime(expireTime)
			.activated(true)
			.build();
		redisTemplate.opsForValue().set(redisKey(accountId, token), authCodeModel, TOKEN_DURATION.plusHours(1));
		return authCodeModel;
	}

	@Override
	public AuthCodeVerifyStat verify(VerifyAuthCodeArgs args) {
		String accountId = args.getAccountId();
		String authCodeToken = args.getAuthCode();
		AuthCodeModel redisAuthCodeModel = (AuthCodeModel) redisTemplate.opsForValue().get(redisKey(accountId, authCodeToken));

		if (redisAuthCodeModel == null) {
			log.info("verify authCodeToken : {} {} is null", accountId, authCodeToken);
			return AuthCodeVerifyStat.FAILED;
		}

		AuthCodeVerifyStat stat = AuthCodeVerifyStat.SUCCESS;

		if (redisAuthCodeModel.getExpireTime() != null && redisAuthCodeModel.getExpireTime().isBefore(LocalDateTime.now())) {
			redisAuthCodeModel.setActivated(false);
			log.info("verify authCodeToken : {} {}  {} is expired", accountId, authCodeToken, redisAuthCodeModel.getExpireTime());
			stat = AuthCodeVerifyStat.EXPIRED;
		}

		if (!redisAuthCodeModel.isActivated()) {
			log.info("verify authCodeToken : {} {} is no active", accountId, authCodeToken);
			stat = AuthCodeVerifyStat.EXPIRED;
		}
		redisAuthCodeModel.setActivated(false);

//		if (redisAuthCodeToken.getIp() != null && redisAuthCodeToken.getIp().equals(args.getIp())) {
//			redisAuthCodeToken.setActivated(false);
//			log.info("verify authCodeToken : {} {}  {} is ip illegal", accountId, authCodeToken, redisAuthCodeToken.getExpireTime());
//			stat = AuthCodeVerifyStat.FAILED;
//		}

		Long expireTimeSeconds = Optional.of(redisAuthCodeModel)
			.map(AuthCodeModel::getExpireTime)
			.map(x -> x.minusSeconds(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8))).toEpochSecond(ZoneOffset.ofHours(8)))
			.orElse(TOKEN_DURATION.toSeconds());

		redisTemplate.opsForValue().set(redisKey(accountId, authCodeToken), redisAuthCodeModel, expireTimeSeconds, TimeUnit.SECONDS);
		return stat;
	}


	public String redisKey(String accountId, String token) {
		return String.format("%s:%s:%S", REDIS_KEY, accountId, token);
	}
}
