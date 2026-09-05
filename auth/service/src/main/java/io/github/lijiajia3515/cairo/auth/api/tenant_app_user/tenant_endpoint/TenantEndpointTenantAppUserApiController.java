package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
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
 * [tenant_endpoint/api] tenant app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_endpoint")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantEndpointTenantAppUserApiController {

	private final TenantEndpointTenantAppUserApiService tenantEndpointTenantAppUserApiService;


	/**
	 * 获取当前应用的终端列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 客户端列表
	 */
	@PostMapping("/get_current_tenant_endpoint_list")
	@PreAuthorize("isAuthenticated()")
	public List<TenantEndpoint> getCurrentEndpointList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @RequestBody(required = false) GetCurrentEndpointArgs args) {
		if (args == null) {
			args = new GetCurrentEndpointArgs();
		}
		return tenantEndpointTenantAppUserApiService.getCurrentTenantEndpointList(principal.getTenantId(), principal.getAppId(), args);
	}

}
