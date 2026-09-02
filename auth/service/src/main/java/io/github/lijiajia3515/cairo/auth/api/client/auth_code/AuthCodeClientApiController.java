package io.github.lijiajia3515.cairo.auth.api.client.auth_code;

import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
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
import java.util.Optional;

/**
 * [client/api] auth code api
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/auth_code")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AuthCodeClientApiController {
	private final AuthCodeClientApiService authCodeClientApiService;

	/**
	 * 客户端 auth_code 校验
	 *
	 * @param args args
	 * @return 是否成功
	 */
	@PostMapping("/verify_auth_code")
	@PreAuthorize("hasAnyAuthority('auth_code:all', 'auth_code:verify_auth_code')")
	public Optional<AuthCodeVerifyStat> verifyAuthCode(@RequestBody VerifyAuthCodeArgs args, HttpServletRequest request) {
//		if (args.getIp() == null) {
//			args.setIp(ServletUtil.getClientIP(request));
//		}
		return Optional.of(authCodeClientApiService.verifyAuthCode(args));
	}
}
