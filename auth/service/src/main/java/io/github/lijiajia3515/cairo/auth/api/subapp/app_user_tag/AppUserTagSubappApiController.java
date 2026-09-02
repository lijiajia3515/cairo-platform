package io.github.lijiajia3515.cairo.auth.api.subapp.app_user_tag;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserMetadataTag;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.CreateAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.DeleteAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.GetAppUserTagArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.GetAppUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.ModifyAppUserTagInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag.ModifyAppUserTagStatusArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.validation.Valid;
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
 * [subapp_user/api] app user tag controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_user_tag")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserTagSubappApiController {

	private final AppUserTagSubappApiService appUserTagSubappApiService;


	/**
	 * 获取标签列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户标签列表
	 */
	@PostMapping("/get_app_user_tag_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')")
	public List<AppUserMetadataTag> getUserList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Valid @Validated @RequestBody(required = false) GetAppUserTagArgs args) {
		if (args == null) {
			args = new GetAppUserTagArgs();
		}
		String appId = principal.getAppId();
		return appUserTagSubappApiService.getAppUserTagList(appId, args);
	}

	/**
	 * 获取用户标签分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户标签分页列表
	 */
	@PostMapping("/get_app_user_tag_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')")
	public Page<AppUserMetadataTag> getUserTagPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													   @RequestBody GetAppUserTagArgs args) {
		if (args == null) {
			args = new GetAppUserTagArgs();
		}
		String appId = principal.getAppId();

		return appUserTagSubappApiService.getAppUserTagPageList(appId, args);
	}

	/**
	 * 用户接口，根据用户标识获取用户信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/get_app_user_tag_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:read')")
	public AppUserMetadataTag getUserTagById(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody GetAppUserTagInfoArgs args) {
		String appId = principal.getAppId();

		return appUserTagSubappApiService.getAppUserTagInfo(appId, args.getTagId());
	}

	/**
	 * 用户接口，创建用户标签
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 创建好的用户
	 */
	@PostMapping("/create_app_user_tag")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:create_app_user_tag')")
	public Optional<Object> createAppUserTag(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody CreateAppUserTagArgs args) {
		String appId = principal.getAppId();
		appUserTagSubappApiService.createAppUserTag(appId, args);
		return Optional.empty();
	}


	/**
	 * 用户接口，修改用户标签信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/modify_app_user_tag_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:modify_app_user_tag_info')")
	public Optional<String> modifyAppUserTagInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												 @Validated @RequestBody ModifyAppUserTagInfoArgs args) {
		appUserTagSubappApiService.modifyAppUserTagInfo(principal.getAppId(), args);
		return Optional.empty();
	}

	/**
	 * 用户接口，修改用户标签状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 用户信息
	 */
	@PostMapping("/modify_app_user_tag_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:modify_app_user_tag_status')")
	public Optional<Object> modifyUserTagStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody ModifyAppUserTagStatusArgs args) {
		String appId = principal.getAppId();
		appUserTagSubappApiService.modifyAppUserTagStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除用户标签
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_app_user_tag")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_user_tag:all', 'app_user_tag:delete_app_user_tag')")
	public Optional<String> deleteAppUserTag(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody DeleteAppUserTagArgs args) {
		String appId = principal.getAppId();
		appUserTagSubappApiService.deleteAppUserTag(appId, args);
		return Optional.empty();
	}

}
