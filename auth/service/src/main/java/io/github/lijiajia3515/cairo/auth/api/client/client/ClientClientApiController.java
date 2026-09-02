package io.github.lijiajia3515.cairo.auth.api.client.client;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
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
 * [client/api] client controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/client")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class ClientClientApiController {

	private final ClientClientApiService clientClientApiService;

	/**
	 * 获取客户端基础列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_basic_client_list")
	@PreAuthorize("hasAnyAuthority('client:all', 'client:read')")
	public List<BasicClient> getAppList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetClientArgs args) {
		return clientClientApiService.getBasicClientList(args);
	}


	/**
	 * 获取客户端列表
	 *
	 * @param args 参数
	 * @return 客户端 列表模式
	 */
	@PostMapping("/get_client_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:read')")
	public List<Client> getClientList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									  @RequestBody GetClientArgs args) {
		return clientClientApiService.getClientList(args);
	}

}
