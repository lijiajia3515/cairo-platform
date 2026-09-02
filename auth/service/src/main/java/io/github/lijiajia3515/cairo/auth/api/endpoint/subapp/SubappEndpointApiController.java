package io.github.lijiajia3515.cairo.auth.api.endpoint.subapp;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.subapp.GetSubappArgs;
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

import java.util.List;

/**
 * [subapp_user/api] app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/subapp")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappEndpointApiController {

	private final SubappEndpointApiService subappEndpointApiService;

	/**
	 * 获取当前用户的子应用列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 子应用列表
	 */
	@PostMapping("/get_subapp_list")
	@PreAuthorize("isAuthenticated()")
	public List<Subapp> getSubappList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @RequestBody(required = false) GetSubappArgs args) {
		if (args == null) {
			args = new GetSubappArgs();
		}
		return subappEndpointApiService.getSubappList(principal.getAppId(), args);
	}
}
