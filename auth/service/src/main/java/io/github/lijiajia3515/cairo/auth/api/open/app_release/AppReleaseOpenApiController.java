package io.github.lijiajia3515.cairo.auth.api.open.app_release;

import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.CheckForUpdateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.CurrentAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetCurrentAppReleasePageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetLatestAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetPreviewAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.OpenAppRelease;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [open/api] app release controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/app_release")
@RequiredArgsConstructor
@BusinessResultBody
public class AppReleaseOpenApiController {

	private final AppReleaseOpenApiService appReleaseOpenApiService;

	/**
	 * 获取最新web发行版应用信息
	 *
	 * @param args args
	 * @return 最新的正式版本
	 */
	@PostMapping("/get_latest_release_web")
	public OpenAppRelease getLatestReleaseWeb(@Validated @RequestBody GetLatestAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestReleaseWeb(args);
	}

	/**
	 * 获取最新android发行版应用信息
	 *
	 * @param args args
	 * @return 最新的正式版本
	 */
	@PostMapping("/get_latest_release_android")
	public OpenAppRelease getLatestReleaseAndroid(@Validated @RequestBody GetLatestAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestReleaseAndroid(args);
	}

	/**
	 * 获取最新ios发行版应用信息
	 *
	 * @param args args
	 * @return 最新的正式版本
	 */
	@PostMapping("/get_latest_release_ios")
	public OpenAppRelease getLatestReleaseIos(@Validated @RequestBody GetLatestAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestReleaseIos(args);
	}

	/**
	 * 获取最新预览版应用信息-web
	 *
	 * @param args args
	 * @return 最新的测试版本
	 */
	@PostMapping("/get_latest_preview_web")
	public OpenAppRelease getLatestPreviewWeb(@Validated @RequestBody GetPreviewAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestPreviewWeb(args);
	}


	/**
	 * 获取最新预览版应用信息-android
	 *
	 * @param args args
	 * @return 最新的测试版本
	 */
	@PostMapping("/get_latest_preview_android")
	public OpenAppRelease getLatestPreviewAndroid(@Validated @RequestBody GetPreviewAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestPreviewAndroid(args);
	}


	/**
	 * 获取最新预览版应用信息-ios
	 *
	 * @param args args
	 * @return 最新的测试版本
	 */
	@PostMapping("/get_latest_preview_ios")
	public OpenAppRelease getLatestPreviewIos(@Validated @RequestBody GetPreviewAppReleaseArgs args) {
		return appReleaseOpenApiService.getLatestPreviewIos(args);
	}

	/**
	 * 检查更新安卓
	 *
	 * @param args args
	 * @return 需要更新的版本
	 */
	@PostMapping("/check_for_updates_android")
	public OpenAppRelease checkForUpdatesAndroid(@Validated @RequestBody CheckForUpdateArgs args) {
		return appReleaseOpenApiService.checkForUpdatesAndroid(args);
	}

	/**
	 * 检查更新ios
	 *
	 * @param args args
	 * @return 需要更新的版本
	 */
	@PostMapping("/check_for_updates_ios")
	public OpenAppRelease checkForUpdatesIos(@Validated @RequestBody CheckForUpdateArgs args) {
		return appReleaseOpenApiService.checkForUpdatesIos(args);
	}

	/**
	 * 查询当前版本记录
	 *
	 * @param args args
	 * @return 字典列表
	 */
	@PostMapping("/get_current_app_release_page_list")
	public Page<CurrentAppRelease> getAppReleasePageList(@Validated @RequestBody GetCurrentAppReleasePageListArgs args) {
		return appReleaseOpenApiService.getCurrentAppReleasePageList(args);
	}
}
