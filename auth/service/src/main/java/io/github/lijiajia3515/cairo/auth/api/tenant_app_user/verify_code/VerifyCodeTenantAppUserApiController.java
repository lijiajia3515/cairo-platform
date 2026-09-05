package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.verify_code;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.modules.captcha.token.VerifyCaptchaToken;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [tenant_endpoint/api] 验证码 controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/verify_code")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class VerifyCodeTenantAppUserApiController {
	private final VerifyCodeTenantAppUserApiService verifyCodeTenantAppUserApiService;

	/**
	 * 发送当前账号手机号验证码
	 *
	 * @return 空
	 */
	@VerifyCaptchaToken
	@PostMapping("/send_my_account_phone_number_verify_code")
	public Optional<String> sendMyAccountPhoneNumberVerifyCode(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		verifyCodeTenantAppUserApiService.sendMyAccountPhoneNumberVerifyCode(principal.getTenantId(), principal.getAppId());
		return Optional.empty();
	}
}
