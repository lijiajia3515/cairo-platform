package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.app_release;


import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.CreateAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.DeleteAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.GetAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.ModifyAppReleaseInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.SetAppReleaseLatestVersionArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.MetadataAppRelease;
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
 * [cairo_web_manage/api] app release controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/app_release")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class AppReleaseCairoWebManageApiController {

	private final AppReleaseCairoWebManageApiService appReleaseCairoWebManageApiService;

	/**
	 * 创建应用版本
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/create_app_release")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:create_app_release')")
	@CairoContext
	public Optional<String> createAppReleaseVersion(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody CreateAppReleaseArgs args) {
		appReleaseCairoWebManageApiService.createAppRelease(args);
		return Optional.empty();
	}

	/**
	 * 更新应用版本信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/modify_app_release_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:modify_app_release_info')")
	public Optional<String> modifyAppReleaseVersionInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														@Validated @RequestBody ModifyAppReleaseInfoArgs args) {
		appReleaseCairoWebManageApiService.modifyAppReleaseInfo(args);
		return Optional.empty();
	}

	/**
	 * 设置为最新版本
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/set_app_release_latest_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:set_app_relase_latest_version')")
	public Optional<String> modifyAppReleaseVersionLatestVersion(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																 @Validated @RequestBody SetAppReleaseLatestVersionArgs args) {
		appReleaseCairoWebManageApiService.setAppReleaseVersionLatestVersion(args);
		return Optional.empty();
	}


	/**
	 * 删除应用版本
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/delete_app_release")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:delete_app_release')")
	public Optional<String> deleteAppRelease(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody DeleteAppReleaseArgs args) {
		appReleaseCairoWebManageApiService.deleteAppRelease(args);
		return Optional.empty();
	}

	/**
	 * 应用版本-查询
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_app_release_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:read')")
	@CairoContext
	public List<MetadataAppRelease> getAppReleaseList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													  @Validated @RequestBody GetAppReleaseArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElse(null);
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElse(null);
		return appReleaseCairoWebManageApiService.getAppReleaseList(appId, endpointId, args);
	}

	/**
	 * 应用版本-分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 字典列表
	 */
	@PostMapping("/get_app_release_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_release:all', 'app_release:read')")
	@CairoContext
	public Page<MetadataAppRelease> getAppReleasePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														  @Validated @RequestBody GetAppReleaseArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElse(null);
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElse(null);
		return appReleaseCairoWebManageApiService.getAppReleasePageList(appId, endpointId, args);
	}

}
