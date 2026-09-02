package io.github.lijiajia3515.cairo.auth.modules.area;

import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaDetailMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.area.GetAreaListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;

import java.util.List;
import java.util.Map;

public interface AreaClientApiService {

	/**
	 * 查询区域列表
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域 list
	 */
	List<Area> getAreaList(GetAreaListArgs args);


	/**
	 * 获取区域详情
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域 optional
	 */
	AreaDetail getAreaDetail(GetAreaDetailArgs args);

	/**
	 * 获取区域详情map
	 * 需要权限 area:read
	 *
	 * @param args 参数
	 * @return 区域map
	 */
	Map<String, AreaDetail> getAreaDetailMap(GetAreaDetailMapArgs args);
}
