package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.login_log.tenant_app_user_login_log;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.login_log.tenant_app_user_login_log.GetMyTenantAppUserLoginLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.login_log.tenant_app_user_login_log.MyTenantAppUserLoginLog;
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

import java.util.Optional;

/**
 * [tenant_endpoint/api] tenant app user login log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_app_user_login_log")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserLoginLogTenantAppUserApiController {

	private final TenantAppUserLoginLogTenantAppUserApiService tenantAppUserLoginLogTenantAppUserApiService;

	/**
	 * 获取我的终端登录日志日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return tenant app user login log page list
	 */
	@PostMapping("/get_my_tenant_app_user_login_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<MyTenantAppUserLoginLog> getMyTenantAppUserLoginLogPageList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
																							@Validated @RequestBody(required = false) GetMyTenantAppUserLoginLogArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantAppUserLoginLogTenantAppUserApiService.getMyTenantAppUserLoginLogPageList(tenantId, appId, userId, Optional.ofNullable(args).orElse(GetMyTenantAppUserLoginLogArgs.builder().build()));
	}

}
