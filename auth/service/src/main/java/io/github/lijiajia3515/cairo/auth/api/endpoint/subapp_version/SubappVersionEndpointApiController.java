package io.github.lijiajia3515.cairo.auth.api.endpoint.subapp_version;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.subapp_version.GetSubappVersionArgs;
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
 * [subapp_version/api] app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/subapp_version")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappVersionEndpointApiController {

	private final SubappVersionEndpointApiService subappVersionEndpointApiService;

	/**
	 * 获取当前用户的子应用版本列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 子应用版本列表
	 */
	@PostMapping("/get_subapp_version_list")
	@PreAuthorize("isAuthenticated()")
	public List<SubappVersion> getSubappVersionList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @RequestBody(required = false) GetSubappVersionArgs args) {
		if (args == null) {
			args = new GetSubappVersionArgs();
		}
		return subappVersionEndpointApiService.getSubappVersionList(args);
	}
}
