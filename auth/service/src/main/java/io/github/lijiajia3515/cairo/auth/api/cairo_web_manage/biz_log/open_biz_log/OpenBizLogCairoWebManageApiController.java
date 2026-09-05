package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.open_biz_log;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.open_biz_log.GetOpenBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.open_biz_log.OpenBizLog;
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
 * [cairo_web_manage/api] open biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/open_biz_log")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class OpenBizLogCairoWebManageApiController {

	private final OpenBizLogCairoWebManageApiService openBizLogCairoWebManageApiService;

	/**
	 * 获取应用级用户业务日志 page list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return open biz log page list
	 */
	@PostMapping("/get_open_biz_log_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'open_biz_log:all', 'open_biz_log:read')")
	@CairoContext
	public Page<OpenBizLog> getClientBizLogPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                                    @Validated @RequestBody(required = false) GetOpenBizLogArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElse(null);

		return openBizLogCairoWebManageApiService.getOpenBizLogPageList(appId, Optional.ofNullable(args).orElse(GetOpenBizLogArgs.builder().build()));
	}
}
