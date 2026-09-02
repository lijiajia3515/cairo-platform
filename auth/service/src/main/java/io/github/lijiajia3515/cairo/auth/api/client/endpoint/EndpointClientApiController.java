package io.github.lijiajia3515.cairo.auth.api.client.endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
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
 * [client/api] app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/endpoint")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class EndpointClientApiController {

	private final EndpointClientApiService endpointClientApiService;

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_list")
	@PreAuthorize("hasAnyAuthority('endpoint:all', 'endpoint:read')")
	public List<Endpoint> getEndpointList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetEndpointClientArgs args) {
		return endpointClientApiService.getEndpointList(args);
	}

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_page_list")
	@PreAuthorize("hasAnyAuthority('endpoint:all', 'endpoint:read')")
	public Page<Endpoint> getEndpointPageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetEndpointClientArgs args) {
		return endpointClientApiService.getEndpointPageList(args);
	}

	/**
	 * 获取app终端列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_list_by_app")
	@PreAuthorize("hasAnyAuthority('endpoint:all', 'endpoint:read')")
	public List<Endpoint> getEndpointByAppList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetEndpointByAppClientArgs args) {
		return endpointClientApiService.getEndpointByAppList(args);
	}

}
