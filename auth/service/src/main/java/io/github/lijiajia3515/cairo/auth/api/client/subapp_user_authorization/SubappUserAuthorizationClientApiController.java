package io.github.lijiajia3515.cairo.auth.api.client.subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;
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

/**
 * [client/api] app subapp user authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/subapp_user_authorization")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class SubappUserAuthorizationClientApiController {

	private final SubappUserAuthorizationClientApiService subappUserAuthorizationClientApiService;

	/**
	 * 获取账号认证
	 *
	 * @param principal 用户凭证才能访问
	 * @return 账号凭证
	 */
	@PostMapping("/get_subapp_user_authorization")
	@PreAuthorize("hasAnyAuthority('subapp_user_authorization:all', 'subapp_user_authorization:get_subapp_user_authorization')")
	public SubappUserAuthorizationModel getAppUserAuthorization(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetSubappUserAuthorizationArgs args) {
		return subappUserAuthorizationClientApiService.getSubappUserAuthorization(args);
	}
}
