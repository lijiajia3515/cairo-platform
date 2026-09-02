package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.MetadataEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.CreateEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.DeleteEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.GetEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.ModifyEndpointInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.endpoint.ModifyEndpointStatusArgs;
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
 * [cairo-web-manage/api] app endpoint controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/endpoint")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class EndpointCairoWebManageApiController {

	private final EndpointCairoWebManageApiService endpointCairoWebManageApiService;

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:read')")
	public List<MetadataEndpoint> getEndpointList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetEndpointArgs args) {
		return endpointCairoWebManageApiService.getEndpointList(args);
	}

	/**
	 * 获取app分页
	 *
	 * @param args 参数
	 * @return app 分页模式
	 */
	@PostMapping("/get_endpoint_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:read')")
	public Page<MetadataEndpoint> getEndpointPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetEndpointArgs args) {
		return endpointCairoWebManageApiService.getEndpointPageList(args);
	}


	/**
	 * create app endpoint
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/create_endpoint")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:create_endpoint')")
	public Optional<String> createEndpoint(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreateEndpointArgs args) {
		endpointCairoWebManageApiService.createEndpoint(args);
		return Optional.empty();
	}

	/**
	 * 修改 app endpoint info
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_endpoint_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:modify_endpoint_info')")
	public Optional<String> modifyEndpointInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyEndpointInfoArgs args) {
		endpointCairoWebManageApiService.modifyEndpointInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_endpoint_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:modify_endpoint_status')")
	public Optional<String> modifyEndpointStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyEndpointStatusArgs args) {
		endpointCairoWebManageApiService.modifyEndpointStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除 app endpoint
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/delete_endpoint")
	@PreAuthorize("hasAnyAuthority('app_admin', 'endpoint:all', 'endpoint:delete_endpoint')")
	public Optional<String> deleteEndpoint(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteEndpointArgs args) {
		endpointCairoWebManageApiService.deleteEndpoint(args);
		return Optional.empty();
	}

}
