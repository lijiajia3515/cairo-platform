package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.MetadataTenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user.TenantAppUserLogoffStatusInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user.ModifyMyTenantAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user.UserInfo;
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

import java.util.Collection;
import java.util.Optional;

/**
 * [tenant_endpoint/api] tenant app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_app_user")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserTenantAppUserApiController {

	private final CairoAuthTenantAppUserService cairoAuthTenantAppUserService;
	private final TenantAppUserTenantAppUserApiService tenantAppUserTenantAppUserApiService;

	/**
	 * 获取当前企业应用级用户信息
	 *
	 * @param principal 1
	 * @return 1
	 */
	@PostMapping("/get_my_tenant_app_user_info")
	@PreAuthorize("isAuthenticated()")
	public UserInfo getMyTenantAppUserInfo(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		return UserInfo.builder()
			// token
			.id(principal.getId())
			.loginType(principal.getLoginType().getValue())
			.snsType(principal.getSnsType())
			// tenant app user
			.tenantId(principal.getTenantId())
			.appId(principal.getAppId())
			.endpointId(principal.getEndpointId())
			.userId(principal.getUserId())
			.nickname(principal.getNickname())
			.phoneNumber(principal.getPhoneNumber())
			.appAdmin(principal.getAppAdmin())
			.tags(principal.getTags())
			.departments(principal.getDepartments())
			.position(principal.getPosition())
			.roles(principal.getRoles())
			// account
			.accountId(principal.getAccountId())
			.accountAvatarUrl(principal.getAccountAvatarUrl())
			.accountNickname(principal.getAccountNickname())
			.accountUsername(principal.getAccountUsername())
			.accountPhoneNumber(principal.getAccountPhoneNumber())
			.accountEmail(principal.getAccountEmail())
			.build();
	}


	/**
	 * 获取当前用户权限集合
	 *
	 * @param principal 凭证
	 * @return 权限字符串
	 */
	@PostMapping("/get_my_tenant_app_user_authority")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyTenantAppUserAuthority(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		return cairoAuthTenantAppUserService.getTenantAppUserAuthorityString(principal.getTenantId(), principal.getAppId(), principal.getEndpointId(), principal.getUserId());
	}


	/**
	 * 获取当前用户功能集合
	 *
	 * @param principal 凭证
	 * @return 功能权限ID集合
	 */
	@PostMapping("/get_my_tenant_app_user_permission_ids")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyTenantAppUserPermissionIds(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		return cairoAuthTenantAppUserService.getTenantAppUserPermissionIds(principal.getTenantId(), principal.getAppId(), principal.getEndpointId(), principal.getUserId());
	}

	/**
	 * 修改用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/modify_my_tenant_app_user_info")
	@PreAuthorize("isAuthenticated()")

	public Optional<String> modifyMyTenantAppUserInfo(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
													  @Validated @RequestBody ModifyMyTenantAppUserInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		tenantAppUserTenantAppUserApiService.modifyMyTenantAppUserInfo(tenantId, appId, userId, args);
		return Optional.empty();
	}

	/**
	 * 获取我的注销状态
	 *
	 * @param principal 凭证
	 * @return 应用级用户信息
	 */
	@PostMapping("/get_my_tenant_app_user_logoff_status")
	@PreAuthorize("isAuthenticated()")
	public TenantAppUserLogoffStatusInfo getMyTenantAppUserLogoffStatus(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantAppUserTenantAppUserApiService.getMyTenantAppUserLogoffStatus(tenantId, appId, userId);
	}

	/**
	 * 获取我的注销状态
	 *
	 * @param principal 凭证
	 * @return 应用级用户信息
	 */
	@PostMapping("/get_my_tenant_app_user_pre_logoff_info")
	@PreAuthorize("isAuthenticated()")
	public PreLogoffInfo getMyTenantAppUserPreLogoffInfo(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return tenantAppUserTenantAppUserApiService.getMyTenantAppUserPreLogoffInfo(tenantId, appId, userId);
	}

	/**
	 * 注销
	 *
	 * @param principal 凭证
	 * @return 用户信息
	 */
	@PostMapping("/logoff_my_tenant_app_user")
	@PreAuthorize("isAuthenticated()")
	public Optional<MetadataTenantAppUser> logoffMyTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		tenantAppUserTenantAppUserApiService.logoffMyTenantAppUser(tenantId, appId, userId);
		return Optional.empty();
	}

	/**
	 * 取消注销
	 *
	 * @param principal 凭证
	 * @return 用户信息
	 */
	@PostMapping("/unlogoff_my_tenant_app_user")
	@PreAuthorize("isAuthenticated()")
	public Optional<MetadataTenantAppUser> unlogoffMyTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		tenantAppUserTenantAppUserApiService.unlogoffMyTenantAppUser(tenantId, appId, userId);
		return Optional.empty();
	}
}
