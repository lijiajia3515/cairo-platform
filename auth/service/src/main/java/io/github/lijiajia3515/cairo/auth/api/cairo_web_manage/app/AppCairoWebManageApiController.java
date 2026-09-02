package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.app;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.MetadataApp;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.CreateAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.DeleteAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.ModifyAppInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app.ModifyAppStatusArgs;
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
 * [cairo-web-manage/api] app controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/app")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppCairoWebManageApiController {

	private final AppCairoWebManageApiService appCairoWebManageApiService;

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_app_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:read')")
	public List<MetadataApp> getAppList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										@Validated @RequestBody GetAppArgs args) {
		return appCairoWebManageApiService.getAppList(args);
	}

	/**
	 * 获取app分页
	 *
	 * @param args 参数
	 * @return app 分页模式
	 */
	@PostMapping("/get_app_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:read')")
	public Page<MetadataApp> getAppPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                            @Validated @RequestBody GetAppArgs args) {
		return appCairoWebManageApiService.getAppPageList(args);
	}


	/**
	 * 创建app
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/create_app")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:create_app')")
	public Optional<String> createApp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									  @Validated @RequestBody CreateAppArgs args) {
		appCairoWebManageApiService.createApp(args);
		return Optional.empty();
	}

	/**
	 * 修改app
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_app_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:modify_app_info')")
	public Optional<String> modifyAppInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody ModifyAppInfoArgs args) {
		appCairoWebManageApiService.modifyAppInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/modify_app_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:modify_app_status')")
	public Optional<String> modifyAppStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody ModifyAppStatusArgs args) {
		appCairoWebManageApiService.modifyAppStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return app
	 */
	@PostMapping("/delete_app")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app:all', 'app:delete_app')")
	public Optional<String> deleteApp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									  @Validated @RequestBody DeleteAppArgs args) {
		appCairoWebManageApiService.deleteApp(args);
		return Optional.empty();
	}

}
