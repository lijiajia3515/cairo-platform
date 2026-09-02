package io.github.lijiajia3515.cairo.auth.modules.captcha.code;

import cn.hutool.captcha.generator.CodeGenerator;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaType;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.Setter;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis 实现 captcha code
 */
@Component
public class RedisCaptchaCodeServiceImpl implements CaptchaCodeService {
	private static final String PREFIX = "captcha:code";

	/**
	 * redisTemplate
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 验证忽略ip
	 */
	@Setter
	private Boolean verifyIp = true;

	public RedisCaptchaCodeServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	@NewSpan
	public void storeCode(CaptchaCode captcha) {
		String key = getCodeKey(captcha.getKey());
		CaptchaCode copyCaptcha = CaptchaCode.builder()
			.type(captcha.getType())
			.code(captcha.getCode())
			.ip(captcha.getIp())
			.expired(false)
			.count(0)
			.build();
		redisTemplate.opsForValue().set(key, copyCaptcha, captcha.getTtl());
	}

	@Override
	@NewSpan
	public void verifyCode(VerifyCaptchaCodeArgs args) {
		String redisCodeKey = getCodeKey(args.getCaptchaKey());
		CaptchaCode captcha = (CaptchaCode) redisTemplate.opsForValue().get(redisCodeKey);
		if (captcha == null) {
			throw new ConflictBusinessException("验证码不存在", CaptchaCodeBusiness.NOT_FOUND);
		}
		if (captcha.isExpired()) {
			throw new ConflictBusinessException("验证码已过期", CaptchaCodeBusiness.EXPIRED);
		}
		if (!verifyCode(captcha, args)) {
			if (captcha.getCount() > 1) {
				redisTemplate.delete(redisCodeKey);
				throw new ConflictBusinessException("失败次数过多，请重新获取验证码", CaptchaCodeBusiness.EXPIRED);
			} else {
				captcha.setCount(captcha.getCount() + 1);
				redisTemplate.opsForValue().set(redisCodeKey, captcha);
				throw new ConflictBusinessException("验证失败", CaptchaCodeBusiness.BAD);
			}
		}

		// 设置验证码code过期
		captcha.setExpired(true);
		captcha.setCount(captcha.getCount() + 1);
		redisTemplate.opsForValue().set(redisCodeKey, captcha);
	}

	private boolean verifyCode(CaptchaCode captcha, VerifyCaptchaCodeArgs args) {
		CairoCaptchaType type = captcha.getType();
		CodeGenerator codeGenerator = CaptchaConstants.getCodeGenerator(type);
		if (!codeGenerator.verify(captcha.getCode(), args.getCaptchaCode())) {
			return false;
		}

		// verifyIp
		if (verifyIp && !args.getIp().equals(captcha.getIp())) {
			return false;
		}

		return true;
	}


	private String getCodeKey(String key) {
		return String.format("%s:%s", PREFIX, key);
	}
}
