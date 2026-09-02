package io.github.lijiajia3515.cairo.auth.api.open.captcha;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaStyle;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaType;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CaptchaWebToken;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.GetCaptchaResponse;
import io.github.lijiajia3515.cairo.auth.modules.captcha.code.VerifyCaptchaCodeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.captcha.GetCaptchaArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * [open/api] captcha controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/captcha")
@RequiredArgsConstructor
@BusinessResultBody
public class CaptchaOpenApiController {
	private final CaptchaOpenApiService captchaOpenApiService;

	/**
	 * 获取行为验证码
	 *
	 * @param args    args
	 * @param request servlet request
	 * @return 响应参数
	 */
	@PostMapping("/get_captcha_code")
	public Optional<GetCaptchaResponse> getCaptchaCode(@Validated @RequestBody GetCaptchaArgs args, HttpServletRequest request) {
		String ip = JakartaServletUtil.getClientIP(request);
		args = Optional.ofNullable(args).orElse(GetCaptchaArgs.builder().build());
		if (args.getType() == null)
			args.setType(CairoCaptchaType.NUMBER);
		if (args.getStyle() == null)
			args.setStyle(CairoCaptchaStyle.values()[RandomUtil.randomInt(CairoCaptchaStyle.values().length)]);
		return Optional.ofNullable(captchaOpenApiService.getCaptchaCode(args, ip));
	}

	/**
	 * 校验行为验证码
	 *
	 * @param args    args
	 * @param request request
	 * @return captcha token
	 */
	@PostMapping("/verify_captcha_code")
	public Optional<CaptchaWebToken> verifyCaptchaCode(@Validated(VerifyCaptchaCodeArgs.Api.class) @RequestBody VerifyCaptchaCodeArgs args, HttpServletRequest request) {
		String ip = JakartaServletUtil.getClientIP(request);
		args.setIp(ip);
		return Optional.of(captchaOpenApiService.verifyCaptchaCode(args));
	}
}
