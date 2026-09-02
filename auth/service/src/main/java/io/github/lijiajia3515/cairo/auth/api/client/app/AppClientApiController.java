package io.github.lijiajia3515.cairo.auth.api.client.app;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
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

/**
 * [client/api] app controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/app")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AppClientApiController {

	private final AppClientApiService appClientApiService;

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_app_list")
	@PreAuthorize("hasAnyAuthority('app:all', 'app:read')")
	public List<App> getAppList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetAppArgs args) {
		return appClientApiService.getAppList(args);
	}

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_app_page_list")
	@PreAuthorize("hasAnyAuthority('app:all', 'app:read')")
	public Page<App> getAppPageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetAppArgs args) {
		return appClientApiService.getAppPageList(args);
	}


}
