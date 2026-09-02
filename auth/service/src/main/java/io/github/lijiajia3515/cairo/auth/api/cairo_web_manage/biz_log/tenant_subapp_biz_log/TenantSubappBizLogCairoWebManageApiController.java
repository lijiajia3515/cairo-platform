package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.tenant_subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.tenant_subapp_biz_log.GetTenantSubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.tenant_subapp_biz_log.TenantSubappBizLog;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
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

import java.util.Optional;

/**
 * [cairo_web_manage/api] tenant app subapp biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/tenant_subapp_biz_log")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantSubappBizLogCairoWebManageApiController {

	private final TenantSubappBizLogCairoWebManageApiService tenantSubappBizLogCairoWebManageApiService;

	/**
	 * 获取企业子应用业务日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return tenant app subapp biz log page list
	 */
	@PostMapping("/get_tenant_subapp_biz_log_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp_biz_log:all', 'tenant_subapp_biz_log:read')")
	@CairoContext
	public Page<TenantSubappBizLog> getTenantAppBizLogPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																		   @Validated @RequestBody(required = false) GetTenantSubappBizLogArgs args) {
		String tenantId = CairoContextHolder.getValue(CairoContextConstants.TENANT_ID).orElseThrow(() -> new ParamsErrorBusinessException("tenantId不能为空"));
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElse(null);

		return tenantSubappBizLogCairoWebManageApiService.getTenantSubappBizLogPageList(tenantId, appId, endpointId, Optional.ofNullable(args).orElse(GetTenantSubappBizLogArgs.builder().build()));
	}
}
