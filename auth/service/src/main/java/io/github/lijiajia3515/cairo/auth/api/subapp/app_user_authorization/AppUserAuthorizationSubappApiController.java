package io.github.lijiajia3515.cairo.auth.api.subapp.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_authorization.AppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_authorization.OfflineAppUserAuthorizationArgs;
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
 * [subapp_user/api] app user authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_user_authorization")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class AppUserAuthorizationSubappApiController {

	private final AppUserAuthorizationSubappApiService subappUserAuthorizationSubappUserApiService;

	/**
	 * 获取应用级用户会话list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 终端会话集合
	 */
	@PostMapping("/get_app_user_authorization_list")
	@PreAuthorize("hasAnyAuthority('app_admin','app_user_authorization:all', 'app_user_authorization:get_app_user_authorization')")
	public List<AppUserAuthorization> getAppUserAuthorizationList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		return subappUserAuthorizationSubappUserApiService.getAppUserAuthorizationList(appId, args);
	}

	/**
	 * 获取应用级用户会话分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 终端会话分页集合
	 */
	@PostMapping("/get_app_user_authorization_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin','app_user_authorization:all', 'app_user_authorization:get_app_user_authorization')")
	public Page<AppUserAuthorization> getAppUserAuthorizationPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		return subappUserAuthorizationSubappUserApiService.getAppUserAuthorizationPageList(appId, args);
	}

	/**
	 * 下线应用级用户会话
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/offline_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_authorization:all', 'app_user_authorization:offline')")
	@CairoContext
	public Optional<String> offlineAppUserAuthorization(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody OfflineAppUserAuthorizationArgs args) {
		String appId = principal.getAppId();
		subappUserAuthorizationSubappUserApiService.offlineAppUserAuthorization(appId, args);
		return Optional.empty();
	}

	/**
	 * 下线所有应用级用户会话
	 *
	 * @param principal 凭证
	 * @return empty
	 */
	@PostMapping("/offline_all_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_authorization:all', 'app_user_authorization:offline_all')")
	@CairoContext
	public Optional<String> offlineAllAccountAuthorization(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		String appId = principal.getAppId();
		subappUserAuthorizationSubappUserApiService.offlineAllAppUserAuthorization(appId);
		return Optional.empty();
	}

}
