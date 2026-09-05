package io.github.lijiajia3515.cairo.auth.api.subapp.app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.CreateAccountAndAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.CreateAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.DeleteAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.GetAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.GetAppUserListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.LogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.ModifyAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.ModifyAppUserStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.TransferAppUserToOtherAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.UnlogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.framework.security.subapp_user.CairoAuthSubappUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
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
 * [subapp_user/api] app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_user")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserSubappApiController {

	private final MenuCommonService menuCommonService;
	private final CairoAuthSubappUserService cairoAuthSubappUserService;
	private final AppUserSubappApiService subappUserApiService;


	/**
	 * 获取当前应用级用户权限集合
	 *
	 * @param principal 凭证
	 * @return 权限字符串
	 */
	@PostMapping("/get_my_subapp_user_authority")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyAppUserAuthority(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		return cairoAuthSubappUserService.getSubappUserAuthorityString(principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion(), principal.getUserId());
	}


	/**
	 * 获取当前应用级用户功能集合
	 *
	 * @param principal 凭证
	 * @return 功能权限ID集合
	 */
	@PostMapping("/get_my_subapp_user_permission_ids")
	@PreAuthorize("isAuthenticated()")
	public Collection<String> getMyAppUserPermissionIds(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		return cairoAuthSubappUserService.getSubappUserPermissionIds(
			principal.getAppId(),
			principal.getEndpointId(),
			principal.getSubappId(),
			principal.getSubappVersion(),
			principal.getUserId()
		);
	}


	/**
	 * 获取我的菜单
	 *
	 * @param principal 凭证
	 * @return 我的资源
	 */
	@PostMapping("/get_my_subapp_user_menu")
	@PreAuthorize("isAuthenticated()")
	public List<MenuNode> getMyAppUserMenu(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		boolean admin = principal.getAppAdmin();
		if (admin) {
			return menuCommonService.getAdminMenu(principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion());
		}
		return menuCommonService.getMyAppUserMenu(principal.getAppId(), principal.getEndpointId(), principal.getSubappId(), principal.getSubappVersion(), principal.getUserId());
	}

	/**
	 * 获取应用级用户列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用级用户列表
	 */
	@PostMapping("/get_app_user_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')")
	public List<AppUserMetadata> getAppUserList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody(required = false) GetAppUserListArgs args) {
		String appId = principal.getAppId();
		return subappUserApiService.getAppUserList(appId, args);
	}

	/**
	 * 获取应用级用户分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用级用户分页列表
	 */
	@PostMapping("/get_app_user_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')")
	public Page<AppUserMetadata> getUserPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												 @Validated @RequestBody GetAppUserListArgs args) {
		String appId = principal.getAppId();
		return subappUserApiService.getUserPageList(appId, args);
	}

	/**
	 * 应用级用户接口，根据应用级用户标识获取应用级用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用级用户信息
	 */
	@PostMapping("/get_app_user_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:read')")
	public AppUserMetadata getUserInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody GetAppUserInfoArgs args) {
		String appId = principal.getAppId();
		return subappUserApiService.getAppUserInfo(appId, args.getUserId());
	}

	/**
	 * 应用级用户接口，创建应用级用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:create_app_user')")
	public Optional<String> createAppUser(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody CreateAppUserArgs args) {
		String appId = principal.getAppId();
		subappUserApiService.createAppUser(appId, args);
		return Optional.empty();
	}

	/**
	 * 创建账号并且创建应用级用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_account_and_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:create_app_user')")
	public Optional<String> createAppUserAndAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody CreateAccountAndAppUserArgs args) {
		String appId = principal.getAppId();
		subappUserApiService.createAccountAndAppUser(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改应用级用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_app_user_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:modify_app_user_info')")
	public Optional<String> modifyAppUserInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody ModifyAppUserInfoArgs args) {
		String appId = principal.getAppId();
		subappUserApiService.modifyAppUserInfo(appId, args);
		return Optional.empty();
	}


	/**
	 * 修改应用级用户状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_app_user_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:modify_app_user_status')")
	public Optional<String> modifyAppUserStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody ModifyAppUserStatusArgs args) {
		String appId = principal.getAppId();
		subappUserApiService.modifyAppUserStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 转移至其他账号
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/transfer_app_user_to_other_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:transfer_app_user_to_other_account')")
	public Optional<String> transferAppUserToOtherAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														  @Validated @RequestBody TransferAppUserToOtherAccountArgs args) {
		String appId = principal.getAppId();
		subappUserApiService.transferAppUserToOtherAccount(appId, args);
		return Optional.empty();
	}

	/**
	 * 注销应用级用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/logoff_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:logoff_app_user')")
	public Optional<String> logoffAppUser(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody LogoffAppUserArgs args) {
		String appId = principal.getAppId();

		subappUserApiService.logoffAppUser(appId, args);
		return Optional.empty();
	}

	/**
	 * 取消注销应用级用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/unlogoff_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:logoff_app_user')")
	public Optional<String> unlogoffAppUser(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody UnlogoffAppUserArgs args) {
		String appId = principal.getAppId();

		subappUserApiService.unlogoffAppUser(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除应用级用户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_app_user")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user:all', 'app_user:delete_app_user')")
	public Optional<String> deleteAppUser(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody DeleteAppUserArgs args) {
		String appId = principal.getAppId();

		subappUserApiService.deleteAppUser(appId, args);
		return Optional.empty();
	}


}
