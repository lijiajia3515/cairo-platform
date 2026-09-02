package io.github.lijiajia3515.cairo.auth.api.tenant_endpoint.client;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.client.GetCurrentAppClientArgs;
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
 * [tenant_app_user/api] client controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/client")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class ClientTenantEndpointApiController {

	private final ClientTenantEndpointApiService clientTenantEndpointApiService;

	/**
	 * 获取当前应用的客户端列表
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return 客户端列表
	 */
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/get_current_app_client_list")
	public List<BasicClient> getCurrentEndpointList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @RequestBody(required = false) GetCurrentAppClientArgs args) {
		if (args == null) {
			args = new GetCurrentAppClientArgs();
		}
		return clientTenantEndpointApiService.getCurrentAppClientList(principal.getAppId(), args);
	}
}
