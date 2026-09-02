package io.github.lijiajia3515.cairo.auth.api.client.area;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
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
import java.util.Map;
import java.util.Optional;

/**
 * [client/api] area controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/area")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
class AreaClientApiController {
	private final AreaClientApiService areaClientApiService;

	/**
	 * 区域列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 区域列表
	 */
	@PostMapping("/get_area_list")
	@PreAuthorize("hasAnyAuthority('area:all', 'area:read')")
	List<Area> getAreaList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody @Validated GetAreaListArgs args) {
		return areaClientApiService.getAreaList(args);
	}

	/**
	 * 区域详情
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 区域详情
	 */
	@PostMapping("/get_area_detail")
	@PreAuthorize("hasAnyAuthority('area:all', 'area:read')")
	Optional<AreaDetail> getAreaDetail(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAreaDetailArgs args) {
		return Optional.ofNullable(areaClientApiService.getAreaDetail(args));
	}

	/**
	 * 区域详情map
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 区域详情map
	 */
	@PostMapping("/get_area_detail_map")
	@PreAuthorize("hasAnyAuthority('area:all', 'area:read')")
	Map<String, AreaDetail> getAreaDetailMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAreaDetailMapArgs args) {
		return areaClientApiService.getAreaDetailMap(args);
	}
}
