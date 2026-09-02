package io.github.lijiajia3515.cairo.auth.api.client.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyService;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * [client/api] auth code service
 */
@Slf4j
@Validated
@Component
public class AuthCodeClientApiService {


	private final AuthCodeVerifyService authCodeVerifyService;

	public AuthCodeClientApiService(AuthCodeVerifyService authCodeVerifyService) {
		this.authCodeVerifyService = authCodeVerifyService;
	}

	@NewSpan
	@BizLog(
		bizId = "auth_code:verify_auth_code",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public AuthCodeVerifyStat verifyAuthCode(@Validated VerifyAuthCodeArgs args) {
		return authCodeVerifyService.verify(args);
	}
}
