package io.github.lijiajia3515.cairo.auth.api.client.captcha;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * [client/api] captcha api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/captcha")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class CaptchaClientApiController {
	private final CaptchaClientApiService captchaClientApiService;

	@PostMapping("/verify_captcha_token")
	@PreAuthorize("hasAnyAuthority('captcha:all', 'captcha:verify_token')")
	public Optional<Boolean> verifyToken(@Validated @NotNull @RequestBody VerifyCaptchaTokenArgs args, HttpServletRequest request) {
		return Optional.of(captchaClientApiService.verifyCaptchaToken(args.getCaptchaToken(), args.getClientIp()));
	}
}
