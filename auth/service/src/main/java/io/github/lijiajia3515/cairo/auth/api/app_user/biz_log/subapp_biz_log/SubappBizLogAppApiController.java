package io.github.lijiajia3515.cairo.auth.api.app_user.biz_log.subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.subapp_biz_log.GetMySubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.subapp_biz_log.MySubappBizLog;
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

import java.util.List;
import java.util.Optional;

/**
 * [endpoint/api] app endpoint biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/subapp_biz_log")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class SubappBizLogAppApiController {

	private final SubappBizLogAppApiService subappBizLogAppApiService;

	/**
	 * 获取我的子应用业务日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return app subapp biz log page list
	 */
	@PostMapping("/get_my_subapp_biz_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<MySubappBizLog> getMySubappBizLogPageList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
                                                                  @Validated @RequestBody(required = false) GetMySubappBizLogArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return subappBizLogAppApiService.getMySubappBizLogPageList(appId, userId, Optional.ofNullable(args).orElse(GetMySubappBizLogArgs.builder().build()));
	}


	/**
	 * 获取我的子应用业务日志 list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return app subapp biz log page list
	 */
	@PostMapping("/get_my_subapp_biz_log_list")
	@PreAuthorize("isAuthenticated()")
	public List<MySubappBizLog> getMySubappBizLogList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
															  @Validated @RequestBody(required = false) GetMySubappBizLogArgs args) {
		String appId = principal.getAppId();
		String userId = principal.getUserId();
		return subappBizLogAppApiService.getMySubappBizLogList(appId, userId, Optional.ofNullable(args).orElse(GetMySubappBizLogArgs.builder().build()));
	}
}
