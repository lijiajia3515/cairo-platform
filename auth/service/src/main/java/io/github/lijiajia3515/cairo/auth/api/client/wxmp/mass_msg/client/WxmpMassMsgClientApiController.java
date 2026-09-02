package io.github.lijiajia3515.cairo.auth.api.client.wxmp.mass_msg.client;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;
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
 * [client/api] wxmpMassMsg controller
 * 微信推文群发
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/wxmp_mass_msg")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpMassMsgClientApiController {
	private final WxmpMassMsgClientApiService wxMassClientApiService;

	@PostMapping("/send")
	@PreAuthorize("hasAnyAuthority('wxmass:all', 'wxmass:send')")
	public WxmpMassMsgResult sendWxmpMassMsg(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody SendWxmpMassMsgArgs args) {
		return wxMassClientApiService.sendWxmpMassMsg(args);
	}

	@PostMapping("/delete")
	@PreAuthorize("hasAnyAuthority('wxmass:all', 'wxmass:send_msg')")
	public Optional<String> deleteWxmpMassMsg(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody DeleteWxmpMassMsgArgs args) {
		wxMassClientApiService.deleteWxmpMassMsg(args);
		return Optional.empty();
	}

}
