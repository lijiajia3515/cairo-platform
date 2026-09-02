package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.client;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.MetadataClient;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.CreateClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.DeleteClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientSecretArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientStatusArgs;
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
import java.util.Optional;

/**
 * [cairo-web-manage/api] client controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/client")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class ClientCairoWebManageApiController {

	private final ClientCairoWebManageApiService clientCairoWebManageApiService;

	/**
	 * 获取客户端列表
	 *
	 * @param args 参数
	 * @return 客户端 列表模式
	 */
	@PostMapping("/get_client_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:read')")
	public List<MetadataClient> getClientList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody GetClientArgs args) {
		return clientCairoWebManageApiService.getClientList(args);
	}

	/**
	 * 获取客户端分页
	 *
	 * @param args 参数
	 * @return 客户端 分页模式
	 */
	@PostMapping("/get_client_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:read')")
	public Page<MetadataClient> getTenantPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody GetClientArgs args) {
		return clientCairoWebManageApiService.getClientPageList(args);
	}


	/**
	 * 创建客户端
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 客户端
	 */
	@PostMapping("/create_client")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:create_client')")
	public Optional<String> createClient(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @Validated @RequestBody CreateClientArgs args) {
		clientCairoWebManageApiService.createClient(args);
		return Optional.empty();
	}

	/**
	 * 修改客户端信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return void
	 */
	@PostMapping("/modify_client_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_info')")
	public Optional<String> modifyClientInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody ModifyClientInfoArgs args) {
		clientCairoWebManageApiService.modifyClientInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改客户端状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return void
	 */
	@PostMapping("/modify_client_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_status')")
	public Optional<String> modifyClientSecret(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											   @Validated @RequestBody ModifyClientStatusArgs args) {
		clientCairoWebManageApiService.modifyClientStatus(args);
		return Optional.empty();
	}

	/**
	 * 修改客户端密钥
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return void
	 */
	@PostMapping("/modify_client_secret")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:modify_client_secret')")
	public Optional<String> modifyClientSecret(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											   @Validated @RequestBody ModifyClientSecretArgs args) {
		clientCairoWebManageApiService.modifyClientSecret(args);
		return Optional.empty();
	}

	/**
	 * 删除客户端
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return void
	 */
	@PostMapping("/delete_client")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client:all', 'client:delete_client')")
	public Optional<String> deleteClient(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @Validated @RequestBody DeleteClientArgs args) {
		clientCairoWebManageApiService.deleteClient(args);
		return Optional.empty();
	}
}
