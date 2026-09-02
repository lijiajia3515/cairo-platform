package io.github.lijiajia3515.cairo.auth.api.client.subapp;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
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
 * [client/api] subapp controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/subapp")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappClientApiController {

	private final SubappClientApiService subappClientApiService;

	/**
	 * 获取子应用列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_subapp_list")
	@PreAuthorize("hasAnyAuthority('subapp:all', 'subapp:read')")
	@CairoContext
	public List<Subapp> getEndpointList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetSubappClientArgs args) {
		if (args.getAppId() == null) args.setAppId(principal.getAppId());
		String appId = principal.getAppId();
		return subappClientApiService.getSubappList(appId, args);
	}


}
