package io.github.lijiajia3515.cairo.core.verifycode;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Validated
public class RedisVerifyCodeServiceImpl implements VerifyCodeService {

	private final static String PREFIX = "verify_code";
	private final static String DEFAULT_BIZ_CODE = "default";
	private final RedisTemplate<String, Object> redisTemplate;

	private final Duration EXPIRED_DURATION = Duration.ofMinutes(5);

	public RedisVerifyCodeServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	@NewSpan("store")
	public void store(@Validated StoreVerifyCodeArgs args) {
		String redisKey = getRedisKey(args.getBizCode(), args.getTarget());
		VerifyCode verifyCode = VerifyCode.builder()
			.verifyCode(args.getVerifyCode())
			.expired(false)
			.failCount(0)
			.build();
		redisTemplate.opsForValue().set(redisKey, verifyCode, EXPIRED_DURATION);
	}

	@Override
	@NewSpan("verify")
	public VerifyCodeStat verify(@Validated VerifyVerifyCodeArgs args) {
		String redisKey = getRedisKey(args.getBizCode(), args.getTarget());
		VerifyCode verifyCode = (VerifyCode) redisTemplate.opsForValue().get(redisKey);
		if (verifyCode == null) {
			return VerifyCodeStat.FAILED;
		}
		long expireTtl = Optional.ofNullable(verifyCode.getExpiredTime()).map(x -> x.toEpochSecond(ZoneOffset.ofHours(8)))
			.map(x -> LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) - x)
			.orElse(EXPIRED_DURATION.toSeconds());
		if (verifyCode.isExpired()) {
			return VerifyCodeStat.EXPIRED;
		}
		if (verifyCode.getFailCount() > args.getMaxFailCount()) {
			verifyCode.setExpired(true);
			redisTemplate.opsForValue().set(redisKey, verifyCode, expireTtl);
			return VerifyCodeStat.FAILED;
		}
		if (!verifyCode.getVerifyCode().equals(args.getVerifyCode())) {
			verifyCode.setFailCount(verifyCode.getFailCount() + 1);
			if (verifyCode.getFailCount() > args.getMaxFailCount()) {
				verifyCode.setExpired(true);
			}
			redisTemplate.opsForValue().set(redisKey, verifyCode);
			return VerifyCodeStat.FAILED;
		}
		verifyCode.setExpired(true);
		redisTemplate.opsForValue().set(redisKey, verifyCode, expireTtl);
		return VerifyCodeStat.SUCCESS;

	}

	@Override
	@NewSpan("expire")
	public void expire(@Validated ExpireVerifyCodeArgs args) {
		String redisKey = getRedisKey(args.getBizCode(), args.getTarget());
		VerifyCode verifyCode = (VerifyCode) redisTemplate.opsForValue().get(redisKey);

		if (verifyCode != null) {
			long expireTtl = Optional.ofNullable(verifyCode.getExpiredTime()).map(x -> x.toEpochSecond(ZoneOffset.ofHours(8)))
				.map(x -> LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) - x)
				.orElse(EXPIRED_DURATION.toSeconds());
			verifyCode.setExpired(true);
			redisTemplate.opsForValue().set(redisKey, verifyCode, expireTtl);
		}

	}

	private String getRedisKey(String bizCode, String target) {
		return String.format("%s:%s:%s", PREFIX, Optional.ofNullable(bizCode).orElse(DEFAULT_BIZ_CODE), target);
	}


}
