package io.github.lijiajia3515.cairo.auth.api.client.verify_code;

import cn.hutool.captcha.generator.RandomGenerator;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.StoreVerifyCodeArgs;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * [client/api] 验证码 service
 */
@Slf4j
@Component
public class VerifyCodeClientApiService {
	private final CairoSecurityProperties cairoSecurityProperties;
	private final VerifyCodeService verifyCodeService;
	private final SmsMsgClientApiService smsMsgClientApiService;
	RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);

	public VerifyCodeClientApiService(VerifyCodeService verifyCodeService,
										   CairoSecurityProperties cairoSecurityProperties,
										   SmsMsgClientApiService smsMsgClientApiService) {
		this.verifyCodeService = verifyCodeService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.smsMsgClientApiService = smsMsgClientApiService;
	}

	/**
	 * 发送验证码给当前账号绑定手机号
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "verify_code:send_account_phone_number_verify_code",
		scope = "read",
		params = {
			@BizLog.Param(key = "phoneNumber", value = "#phoneNumber"),
		}
	)
	public void sendAccountPhoneNumberVerifyCode(@Valid @NotNull String phoneNumber) {
		String verifyCode = randomGenerator.generate();
		StoreVerifyCodeArgs verifyCodeArgs = StoreVerifyCodeArgs.builder()
			.bizCode(CairoAuthVerifyCodeConstants.AUTH)
			.target(phoneNumber)
			.verifyCode(verifyCode)
			.build();
		verifyCodeService.store(verifyCodeArgs);
		log.debug("发送验证码： 手机号：{} 验证码: {}.", phoneNumber, verifyCode);
		smsMsgClientApiService.sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
			.phoneNumber(phoneNumber)
			.appId(cairoSecurityProperties.getCairoAppId())
			.bizId(CairoAuthSmsConstants.VerifyCode.BIZ_ID)
			.args(Collections.singletonMap(CairoAuthSmsConstants.VerifyCode.PARAM_CODE, verifyCode))
			.build()
		);
	}
}
