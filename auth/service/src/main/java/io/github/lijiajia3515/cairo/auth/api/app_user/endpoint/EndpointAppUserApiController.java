package io.github.lijiajia3515.cairo.auth.api.app_user.endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.endpoint.GetEndpointArgs;
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
 * [endpoint/api] app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/endpoint")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class EndpointAppUserApiController {

	private final EndpointAppUserApiService endpointAppUserApiService;

	/**
	 * 获取当前用户的终端列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 客户端列表
	 */
	@PostMapping("/get_endpoint_list")
	@PreAuthorize("isAuthenticated()")
	public List<Endpoint> getEndpointList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @RequestBody(required = false) GetEndpointArgs args) {
		if (args == null) {
			args = new GetEndpointArgs();
		}
		return endpointAppUserApiService.getEndpointList(principal.getAppId(), args);
	}
}
