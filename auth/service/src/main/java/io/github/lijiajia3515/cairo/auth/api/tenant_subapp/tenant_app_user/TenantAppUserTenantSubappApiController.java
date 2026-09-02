package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user.CairoAuthTenantSubappUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.MetadataTenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.CreateAccountAndTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.CreateTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.DeleteTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.GetTenantAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.GetTenantAppUserListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.LogoffTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.ModifyTenantAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.ModifyTenantAppUserStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.TransferTenantAppUserToOtherAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.UnlogoffTenantAppUserArgs;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * [tenant_subapp_user/api] tenant app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_user")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserTenantSubappApiController {

	private final MenuCommonService menuCommonService;
	private final CairoAuthTenantSubappUserService cairoAuthTenantSubappUserService;

	private final TenantAppUserTenantSubappApiService tenantAppUserTenantSubappApiService;

	/**
	 * 获取我的菜单
	 *
	 * @param principal 凭证
	 * @return 我的资源
	 */
	@PostMapping("/get_my_tenant_subapp_user_menu")
	@PreAuthorize("isAuthenticated()")
	public List<MenuNode> getMyTenantSubappUserMenu(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal) {
		boolean admin = principal.getAppAdmin();
		if (admin) {
			return menuCommonService.getAdminMenu(principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion());
		}
		return menuCommonService.getMyTenantSubappUserMenu(principal.getTenantId(), principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion(), principal.getUserId());
	}

	/**
	 * 获取当前用户权限集合
	 *
	 * @param principal 凭证
	 * @return 权限字符串
	 */
	@PostMapping("/get_my_tenant_subapp_user_authority")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyTenantSubappUserAuthority(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal) {
		return cairoAuthTenantSubappUserService.getTenantSubappUserAuthorityString(principal.getTenantId(),
			principal.getAppId(), principal.getEndpointId(),
			principal.getSubappId(), principal.getSubappVersion(),
			principal.getUserId()
		);
	}


	/**
	 * 获取当前用户功能集合
	 *
	 * @param principal 凭证
	 * @return 功能权限ID集合
	 */
	@PostMapping("/get_my_tenant_subapp_user_permission_ids")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyTenantSubappUserPermissionIds(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal) {
		return cairoAuthTenantSubappUserService.getTenantSubappUserPermissionIds(principal.getTenantId(), principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion(), principal.getUserId());
	}


	/**
	 * 获取用户列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户列表
	 */
	@PostMapping("/get_tenant_app_user_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')")
	public List<MetadataTenantAppUser> getTenantAppUserList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
                                                            @Validated @RequestBody(required = false) GetTenantAppUserListArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppUserTenantSubappApiService.getTenantAppUserList(tenantId, appId, args);
	}

	/**
	 * 获取用户分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户分页列表
	 */
	@PostMapping("/get_tenant_app_user_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')")
	public Page<MetadataTenantAppUser> getTenantAppUserPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
                                                                @Validated @RequestBody GetTenantAppUserListArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppUserTenantSubappApiService.getTenantAppUserPageList(tenantId, appId, args);
	}

	/**
	 * 用户接口，根据用户标识获取用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/get_tenant_app_user_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:read')")
	public MetadataTenantAppUser getTenantAppUserInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
                                                      @Validated @RequestBody GetTenantAppUserInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppUserTenantSubappApiService.getTenantAppUserInfo(tenantId, appId, args.getUserId());
	}

	/**
	 * 用户接口，创建用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_tenant_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:create_tenant_app_user')")
	public Optional<String> createTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												@Validated @RequestBody CreateTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTenantSubappApiService.createTenantAppUser(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 创建账号并且创建用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_account_and_tenant_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:create_tenant_app_user')")
	public Optional<String> createAccountAndTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														  @Validated @RequestBody CreateAccountAndTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTenantSubappApiService.createAccountAndTenantAppUser(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_user_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:modify_tenant_app_user_info')")
	public Optional<String> modifyTenantAppUserInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													@Validated @RequestBody ModifyTenantAppUserInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTenantSubappApiService.modifyTenantAppUserInfo(tenantId, appId, args);
		return Optional.empty();
	}


	/**
	 * 修改用户状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_user_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenan_user:all', 'tenant_app_user:modify_tenant_app_user_status')")
	public Optional<String> modifyTenantAppUserStatus(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													  @Validated @RequestBody ModifyTenantAppUserStatusArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTenantSubappApiService.modifyTenantAppUserStatus(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 转移至其他账号
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/transfer_tenant_app_user_to_other_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:transfer_tenant_app_user_to_other_account')")
	public Optional<String> transferTenantAppUserToOtherAccount(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																@Validated @RequestBody TransferTenantAppUserToOtherAccountArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserTenantSubappApiService.transferTenantAppUserToOtherAccount(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 注销用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/logoff_tenant_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:logoff_tenant_app_user')")
	public Optional<String> logoffTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												@Validated @RequestBody LogoffTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppUserTenantSubappApiService.logoffTenantAppUser(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 取消注销用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/unlogoff_tenant_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:unlogoff_tenant_app_user')")
	public Optional<String> unlogoffTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												  @Validated @RequestBody UnlogoffTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppUserTenantSubappApiService.unlogoffTenantAppUser(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 删除用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user:all', 'tenant_app_user:delete_tenant_app_user')")
	public Optional<String> deleteTenantAppUser(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											 @Validated @RequestBody DeleteTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppUserTenantSubappApiService.deleteTenantAppUser(tenantId, appId, args);
		return Optional.empty();
	}
}
