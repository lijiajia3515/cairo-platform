package io.github.lijiajia3515.cairo.auth.api.open.verify_code;

import cn.hutool.captcha.generator.RandomGenerator;
import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.StoreVerifyCodeArgs;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.domain.api.open.verify_code.SendSmsVerifyCodeArgs;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * [open/api] 验证码 service
 */
@Slf4j
@Component
public class VerifyCodeOpenApiService {
	private final CairoSecurityProperties cairoSecurityProperties;
	private final VerifyCodeService verifyCodeService;
	private final SmsMsgClientApiService smsMsgClientApiService;
	RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);

	public VerifyCodeOpenApiService(RabbitTemplate rabbitTemplate, VerifyCodeService verifyCodeService, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper, CairoSecurityProperties cairoSecurityProperties, SmsMsgClientApiService smsMsgClientApiService) {
		this.verifyCodeService = verifyCodeService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.smsMsgClientApiService = smsMsgClientApiService;
	}

	/**
	 * 发送短信验证码
	 *
	 * @param args 参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "verify_code:send_verify_code_sms",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "verify_code", keys = {"#args.phoneNumber"}, autoRelease = false, expire = 60000, acquireTimeout = 1000)
	public void sendVerifyCodeSms(SendSmsVerifyCodeArgs args) {
		String verifyCode = randomGenerator.generate();
		StoreVerifyCodeArgs verifyCodeArgs = StoreVerifyCodeArgs.builder()
			.bizCode(CairoAuthVerifyCodeConstants.AUTH)
			.target(args.getPhoneNumber())
			.verifyCode(verifyCode)
			.build();
		verifyCodeService.store(verifyCodeArgs);
		log.debug("发送验证码： 手机号：{} 验证码: {}.", args.getPhoneNumber(), verifyCode);
		smsMsgClientApiService.sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
			.phoneNumber(args.getPhoneNumber())
			.appId(cairoSecurityProperties.getCairoAppId())
			.bizId(CairoAuthSmsConstants.VerifyCode.BIZ_ID)
			.args(Collections.singletonMap(CairoAuthSmsConstants.VerifyCode.PARAM_CODE, verifyCode))
			.build()
		);
	}
}
