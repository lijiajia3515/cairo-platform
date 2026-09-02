package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.login_log.client_login_log;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.client_login_log.ClientLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.client_login_log.GetClientLoginLogArgs;
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
 * [cairo_web_manage/api] client login log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/client_login_log")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class ClientLoginLogCairoWebManageApiController {

	private final ClientLoginLogCairoWebManageApiService clientLoginLogCairoWebManageApiService;


	/**
	 * 获取客户端登录日志分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return client login log page list
	 */
	@PostMapping("/get_client_login_log_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'client_login_log:all', 'client_login_log:read')")
	@CairoContext
	public Page<ClientLoginLog> getClientLoginLogPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														  @Validated @RequestBody(required = false) GetClientLoginLogArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElse(null);
		String clientId = CairoContextHolder.getValue(CairoContextConstants.CLIENT_ID).orElse(null);

		return clientLoginLogCairoWebManageApiService.getClientLoginLogPageList(appId, clientId, Optional.ofNullable(args).orElse(GetClientLoginLogArgs.builder().build()));
	}

}
