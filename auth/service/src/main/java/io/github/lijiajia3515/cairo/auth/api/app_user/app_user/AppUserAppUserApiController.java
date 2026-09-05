package io.github.lijiajia3515.cairo.auth.api.app_user.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.AppUserLogoffStatusInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.PreLogoffInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.ModifyMyAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.UserInfo;
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
 * [endpoint/api] app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/app_user")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserAppUserApiController {

	private final AppUserAppUserApiService appUserAppUserApiService;

	/**
	 * 获取当前应用级用户信息
	 *
	 * @param principal 1
	 * @return 1
	 */
	@PostMapping("/get_my_app_user_info")
	@PreAuthorize("isAuthenticated()")
	public UserInfo getMyAppUserInfo(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		return UserInfo.builder()
			// token
			.id(principal.getId())
			.loginType(principal.getLoginType().getValue())
			.snsType(principal.getSnsType())
			// app_user
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
	 * 修改应用级用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用级用户信息
	 */
	@PostMapping("/modify_my_app_user_info")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAppUserInfo(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
												@Validated @RequestBody ModifyMyAppUserInfoArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		appUserAppUserApiService.modifyMyAppUserInfo(appId, userId, args);
		return Optional.empty();
	}

	/**
	 * 获取我的注销状态
	 *
	 * @param principal 凭证
	 * @return 应用级用户信息
	 */
	@PostMapping("/get_my_app_user_logoff_status")
	@PreAuthorize("isAuthenticated()")
	public AppUserLogoffStatusInfo getMyAppUserLogoffStatus(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return appUserAppUserApiService.getMyAppUserLogoffStatus(appId, userId);
	}

	/**
	 * 获取我的注销状态
	 *
	 * @param principal 凭证
	 * @return 应用级用户信息
	 */
	@PostMapping("/get_my_app_user_pre_logoff_info")
	@PreAuthorize("isAuthenticated()")
	public PreLogoffInfo getMyAppUserPreLogoffInfo(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return appUserAppUserApiService.getMyAppUserPreLogoffInfo(appId, userId);
	}

	/**
	 * 注销
	 *
	 * @param principal 凭证
	 * @return 应用级用户信息
	 */
	@PostMapping("/logoff_my_app_user")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> logoffMyAppUser(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		appUserAppUserApiService.logoffMyAppUser(appId, userId);
		return Optional.empty();
	}

	/**
	 * 取消注销
	 *
	 * @param principal 凭证
	 * @return 用户信息
	 */
	@PostMapping("/unlogoff_my_app_user")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> unlogoffMyAppUser(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		appUserAppUserApiService.unlogoffMyAppUser(appId, userId);
		return Optional.empty();
	}

}
