package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.area;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.CreateAreaArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.DeleteAreaArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.GetAreaPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaHotArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.ModifyAreaStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area.MoveAreaArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataArea;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataAreaDetail;
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
 * [cairo_web_manage/api] area controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/area")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class AreaCairoWebManageApiController {
	private final AreaCairoWebManageApiService areaCairoWebManageApiService;

	/**
	 * 获取区域分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_area_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:read')")
	public Page<MetadataArea> getAreaList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAreaPageListArgs args) {
		return areaCairoWebManageApiService.getAreaPageList(args);
	}

	/**
	 * 获取区域详情
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_area_detail")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:read')")
	public MetadataAreaDetail getAreaDetail(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAreaDetailArgs args) {
		return areaCairoWebManageApiService.getAreaDetail(args);
	}

	/**
	 * 创建区域
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/create_area")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:create_area')")
	public Optional<String> createArea(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreateAreaArgs args) {
		areaCairoWebManageApiService.createArea(args);
		return Optional.empty();
	}

	/**
	 * 修改区域信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_area_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:modify_area_info')")
	public Optional<String> modifyAreaInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAreaInfoArgs args) {
		areaCairoWebManageApiService.modifyAreaInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改区域热门
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_area_hot")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:modify_area_hot')")
	public Optional<String> modifyAreaHot(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAreaHotArgs args) {
		areaCairoWebManageApiService.modifyAreaHot(args);
		return Optional.empty();
	}

	/**
	 * 修改区域状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_area_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:read')")
	public Optional<String> modifyAreaStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAreaStatusArgs args) {
		areaCairoWebManageApiService.modifyAreaStatus(args);
		return Optional.empty();
	}

	/**
	 * 移动区域
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/move_area")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:move_area')")
	public Optional<String> moveArea(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody MoveAreaArgs args) {
		areaCairoWebManageApiService.moveArea(args);
		return Optional.empty();
	}

	/**
	 * 删除区域
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/delete_area")
	@PreAuthorize("hasAnyAuthority('app_admin', 'area:all', 'area:read')")
	public Optional<String> deleteArea(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteAreaArgs args) {
		areaCairoWebManageApiService.deleteArea(args);
		return Optional.empty();
	}
}
