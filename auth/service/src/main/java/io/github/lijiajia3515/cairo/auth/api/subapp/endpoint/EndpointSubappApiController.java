package io.github.lijiajia3515.cairo.auth.api.subapp.endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.endpoint.GetEndpointArgs;
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
@RequestMapping("/subapp_user_api/endpoint")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class EndpointSubappApiController {

	private final EndpointSubappApiService endpointSubappApiService;

	/**
	 * 获取终端列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 客户端列表
	 */
	@PostMapping("/get_endpoint_list")
	@PreAuthorize("isAuthenticated()")
	public List<Endpoint> getEndpointList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestBody(required = false) GetEndpointArgs args) {
		if (args == null) {
			args = new GetEndpointArgs();
		}
		return endpointSubappApiService.getEndpointList(principal.getAppId(), args);
	}
}
