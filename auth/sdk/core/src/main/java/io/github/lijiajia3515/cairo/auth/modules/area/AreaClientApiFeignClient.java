package io.github.lijiajia3515.cairo.auth.modules.area;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * client-api area feign client
 */
@FeignClient(
	contextId = "areaClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/area",
	fallbackFactory = AreaClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface AreaClientApiFeignClient {

	/**
	 * 查询区域列表
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域 list
	 */
	@PostMapping("/get_area_list")
	ResponseEntity<BusinessResult<List<Area>>> getAreaList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                           @RequestBody GetAreaListArgs args);


	/**
	 * 获取区域详情
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域 optional
	 */
	@PostMapping("/get_area_detail")
	ResponseEntity<BusinessResult<AreaDetail>> getAreaDetail(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                             @RequestBody GetAreaDetailArgs args);

	/**
	 * 获取区域详情map
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域map
	 */
	@PostMapping("/get_area_detail_map")
	ResponseEntity<BusinessResult<Map<String, AreaDetail>>> getAreaDetailMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			 @RequestBody GetAreaDetailMapArgs args);
}
