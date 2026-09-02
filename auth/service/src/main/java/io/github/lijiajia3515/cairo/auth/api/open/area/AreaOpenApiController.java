package io.github.lijiajia3515.cairo.auth.api.open.area;

import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetAreaTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.area.GetCityListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaTree;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * [open/api] area controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/area")
@BusinessResultBody
@RequiredArgsConstructor
class AreaOpenApiController {
	private final AreaOpenApiService areaOpenApiService;


	/**
	 * 获取区域列表
	 *
	 * @param args 参数
	 * @return 区域列表
	 */
	@PostMapping("/get_area_list")
	List<Area> getAreaList(@RequestBody @Validated GetAreaListArgs args) {
		return areaOpenApiService.getAreaList(args);
	}

	/**
	 * 获取城市列表
	 * @param args 参数
	 * @return 区域列表集合
	 */
	@PostMapping("/get_city_list")
	List<Area> getCityList(@RequestBody @Validated GetCityListArgs args){
		return areaOpenApiService.getCityList(args);
	}

	/**
	 * 获取区域列表
	 *
	 * @param args 参数
	 * @return 区域列表
	 */
	@PostMapping("/get_area_tree_list")
	List<AreaTree> getAreaTreeList(@RequestBody @Validated GetAreaTreeArgs args) {
		return areaOpenApiService.getAreaTreeList(args);
	}


	/**
	 * 获取区域详情
	 *
	 * @param args 参数
	 * @return 区域详情
	 */
	@PostMapping("/get_area_detail")
	Optional<AreaDetail> getAreaDetail(@Validated @RequestBody GetAreaDetailArgs args) {
		return Optional.ofNullable(areaOpenApiService.getAreaDetail(args));
	}
}
