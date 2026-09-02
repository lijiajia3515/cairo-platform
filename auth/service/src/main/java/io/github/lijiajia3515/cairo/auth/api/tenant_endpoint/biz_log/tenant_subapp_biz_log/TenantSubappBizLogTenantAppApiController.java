package io.github.lijiajia3515.cairo.auth.api.tenant_endpoint.biz_log.tenant_subapp_biz_log;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.biz_log.tenant_subapp_biz_log.GetMyTenantSubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.biz_log.tenant_subapp_biz_log.MyTenantSubappBizLog;
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
 * [tenant_endpoint/api]tenant app endpoint biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_subapp_biz_log")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantSubappBizLogTenantAppApiController {

	private final TenantSubappBizLogTenantAppApiService tenantSubappBizLogTenantAppApiService;

	/**
	 * 获取我的子应用业务日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return tenant app subapp
	 * biz log page list
	 */
	@PostMapping("/get_my_tenant_subapp_biz_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<MyTenantSubappBizLog> getMyTenantSubappBizLogPageList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
																			  @Validated @RequestBody(required = false) GetMyTenantSubappBizLogArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantSubappBizLogTenantAppApiService.getMyTenantSubappBizLogPageList(tenantId, appId, userId, Optional.ofNullable(args).orElse(GetMyTenantSubappBizLogArgs.builder().build()));
	}

	/**
	 * 获取我的子应用业务日志 list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return tenant app subapp biz log page list
	 */
	@PostMapping("/get_my_tenant_subapp_biz_log_list")
	@PreAuthorize("isAuthenticated()")
	public List<MyTenantSubappBizLog> getMyTenantSubappBizLogList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
																		  @Validated @RequestBody(required = false) GetMyTenantSubappBizLogArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantSubappBizLogTenantAppApiService.getMyTenantSubappBizLogList(tenantId, appId, userId, Optional.ofNullable(args).orElse(GetMyTenantSubappBizLogArgs.builder().build()));
	}
}
