package io.github.lijiajia3515.cairo.auth.api.endpoint.login_log.app_user_login_log;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.login_log.app_user_login_log.GetMyAppUserLoginLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.login_log.app_user_login_log.MyAppUserLoginLog;
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
 * [endpoint/api] app endpoint user login log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/app_user_login_log")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserLoginLogEndpointApiController {

	private final AppUserLoginLogEndpointApiService appUserLoginLogEndpointApiService;

	/**
	 * 获取我的终端登录日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return endpoint user login log page list
	 */
	@PostMapping("/get_my_app_user_login_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<MyAppUserLoginLog> getMyAppUserLoginLogPageList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
																				@Validated @RequestBody(required = false) GetMyAppUserLoginLogArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return appUserLoginLogEndpointApiService.getMyAppUserLoginLogPageList(appId, userId, Optional.ofNullable(args).orElse(GetMyAppUserLoginLogArgs.builder().build()));
	}

}
