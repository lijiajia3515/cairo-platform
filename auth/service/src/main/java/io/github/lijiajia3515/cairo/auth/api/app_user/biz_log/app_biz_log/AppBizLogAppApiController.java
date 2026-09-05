package io.github.lijiajia3515.cairo.auth.api.app_user.biz_log.app_biz_log;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.biz_log.app_biz_log.GetMyAppBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.biz_log.app_biz_log.MyAppBizLog;
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
 * [endpoint/api] app endpoint biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/app_biz_log")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppBizLogAppApiController {

	private final AppBizLogAppApiService endpointBizLogAppApiService;

	/**
	 * 获取我的业务日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return app endpoint biz log page list
	 */
	@PostMapping("/get_my_app_biz_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<MyAppBizLog> getMyAppBizLogPageList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
                                                                    @Validated @RequestBody(required = false) GetMyAppBizLogArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return endpointBizLogAppApiService.getMyAppBizLogPageList(appId, userId, Optional.ofNullable(args).orElse(GetMyAppBizLogArgs.builder().build()));
	}
}
