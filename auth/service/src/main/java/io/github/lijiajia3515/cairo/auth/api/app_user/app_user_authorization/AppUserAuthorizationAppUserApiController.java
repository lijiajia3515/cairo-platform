package io.github.lijiajia3515.cairo.auth.api.app_user.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_authorization.AppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user_authorization.GetMyAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user_authorization.OfflineMyAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user_authorization.RegisterDeviceArgs;
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
 * [endpoint/api] app user authorization controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/app_user_authorization")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserAuthorizationAppUserApiController {

	private final AppUserAuthorizationAppUserApiService appUserAuthorizationAppUserApiService;

	/**
	 * 获取我的应用级用户会话列表
	 *
	 * @param principal 1
	 * @return AppUserAuthorization list
	 */
	@PostMapping("/get_my_app_user_authorization_list")
	@PreAuthorize("isAuthenticated()")
	public List<AppUserAuthorization> getMyAppUserAuthorizationList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody(required = false) GetMyAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return appUserAuthorizationAppUserApiService.getMyAppUserAuthorizationList(appId, userId, args);
	}

	/**
	 * 获取我的应用级用户会话分页列表
	 *
	 * @param principal 1
	 * @return AppUserAuthorization page
	 */
	@PostMapping("/get_my_app_user_authorization_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<AppUserAuthorization> getMyAppUserAuthorizationPageList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody(required = false) GetMyAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return appUserAuthorizationAppUserApiService.getMyAppUserAuthorizationPageList(appId, userId, args);
	}

	/**
	 * 注册设备
	 *
	 * @param principal 凭证
	 */
	@PostMapping("/register_my_app_user_device")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> registerMyAppUserDevice(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody RegisterDeviceArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		String userId = principal.getUserId();
		String tokenId = principal.getId();
		appUserAuthorizationAppUserApiService.registerMyAppUserDevice(appId, endpointId, userId, tokenId, args);
		return Optional.empty();
	}

	/**
	 * 下线我的应用级用户会话
	 *
	 * @param principal 凭证
	 */
	@PostMapping("/offline_my_app_user_authorization")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> offlineMyAppUserAuthorization(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody(required = false) OfflineMyAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		appUserAuthorizationAppUserApiService.offlineMyAppUserAuthorization(appId, userId, args);
		return Optional.empty();
	}

	/**
	 * 退出登录
	 *
	 * @param principal 凭证
	 */
	@PostMapping("/logout_app_user_authorization")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> logoutAppUserAuthorization(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		String appId = principal.getAppId();
		String tokenId = principal.getId();
		appUserAuthorizationAppUserApiService.logoutAppUserAuthorization(appId, tokenId);
		return Optional.empty();
	}

}
