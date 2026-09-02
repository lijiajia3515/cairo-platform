package io.github.lijiajia3515.cairo.auth.api.client.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_endpoint.GetCurrentEndpointArgs;
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
 * [client/api] tenant app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_endpoint")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantEndpointClientApiController {

	private final TenantEndpointClientApiService tenantEndpointClientApiService;


	/**
	 * 获取当前企业应用的终端列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 企业终端列表
	 */
	@PostMapping("/get_tenant_endpoint_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')")
	public List<TenantEndpoint> getCurrentEndpointList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody(required = false) GetCurrentEndpointArgs args) {
		if (args == null) {
			args = new GetCurrentEndpointArgs();
		}
		return tenantEndpointClientApiService.getCurrentTenantEndpointList(args.getTenantId(), principal.getAppId(), args);
	}

}
