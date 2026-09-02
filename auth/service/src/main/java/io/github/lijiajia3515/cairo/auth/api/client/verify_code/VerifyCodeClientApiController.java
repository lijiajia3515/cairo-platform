package io.github.lijiajia3515.cairo.auth.api.client.verify_code;

import io.github.lijiajia3515.cairo.auth.domain.api.client.verify_code.SendAccountPhoneNumberVerifyCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [client/api] 验证码 controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/verify_code")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class VerifyCodeClientApiController {
	private final VerifyCodeClientApiService verifyCodeClientApiService;

	/**
	 * 发送账号手机号验证码
	 *
	 * @return 空
	 */
	@PostMapping("/send_account_phone_number_verify_code")
	@PreAuthorize("hasAnyAuthority('verify_code:all', 'verify_code:send_account_phone_number_verify_code')")
	public Optional<String> sendAccountPhoneNumberVerifyCode(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated@RequestBody SendAccountPhoneNumberVerifyCodeArgs args) {
		verifyCodeClientApiService.sendAccountPhoneNumberVerifyCode(args.getPhoneNumber());
		return Optional.empty();
	}
}
