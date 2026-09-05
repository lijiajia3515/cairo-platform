package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_authorization.TenantAppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.GetMyTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.OfflineMyTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.RegisterDeviceArgs;
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
 * [tenant_endpoint/api] tenant app user authorization controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_app_user_authorization")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserAuthorizationTenantAppUserApiController {

	private final TenantAppUserAuthorizationTenantAppUserApiService tenantAppUserAuthorizationTenantAppUserApiService;

	/**
	 * 获取我的企业应用级用户会话列表
	 *
	 * @param principal 1
	 * @return TenantAppUserAuthorization list
	 */
	@PostMapping("/get_my_tenant_app_user_authorization_list")
	@PreAuthorize("isAuthenticated()")
	public List<TenantAppUserAuthorization> getMyTenantAppUserAuthorizationList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantAppUserAuthorizationTenantAppUserApiService.getMyTenantAppUserAuthorizationList(tenantId, appId, userId);
	}

	/**
	 * 获取我的企业应用级用户会话分页列表
	 *
	 * @param principal 1
	 * @return TenantAppUserAuthorization page
	 */
	@PostMapping("/get_my_tenant_app_user_authorization_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<TenantAppUserAuthorization> getMyTenantAppUserAuthorizationPageList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody(required = false) GetMyTenantAppUserAuthorizationArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantAppUserAuthorizationTenantAppUserApiService.getMyTenantAppUserAuthorizationPageList(tenantId, appId, userId, args);
	}

	/**
	 * 注册我的设备
	 *
	 * @param principal 1
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/register_my_tenant_app_user_device")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> registerMyDevice(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody RegisterDeviceArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		String endpointId = principal.getEndpointId();
		String tokenId = principal.getId();
		tenantAppUserAuthorizationTenantAppUserApiService.registerMyTenantAppUserDevice(tenantId, appId, endpointId, userId, tokenId, args);
		return Optional.empty();
	}


	/**
	 * 下线我的企业应用级用户会话
	 *
	 * @param principal 凭证
	 */
	@PostMapping("/offline_my_tenant_app_user_authorization")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> offlineMyTenantAppUserAuthorization(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody(required = false) OfflineMyTenantAppUserAuthorizationArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		tenantAppUserAuthorizationTenantAppUserApiService.offlineMyTenantAppUserAuthorization(tenantId, appId, userId, args);
		return Optional.empty();
	}

	/**
	 * 退出登录
	 *
	 * @param principal 凭证
	 */
	@PostMapping("/logout_tenant_app_user_authorization")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> logoutTenantAppUserAuthorization(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String tokenId = principal.getId();
		tenantAppUserAuthorizationTenantAppUserApiService.logoutTenantAppUserAuthorization(tenantId, appId, tokenId);
		return Optional.empty();
	}

}
