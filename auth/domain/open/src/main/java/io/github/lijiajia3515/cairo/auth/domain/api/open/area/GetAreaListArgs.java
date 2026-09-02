package io.github.lijiajia3515.cairo.auth.domain.api.open.area;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取区域列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAreaListArgs {
	/**
	 * 上级区域ID， 默认查询省份
	 */
	private String parentAreaId;

	/**
	 * 开启简称
	 */
	private boolean enableShort;

	/**
	 * 过滤启用
	 */
	private Boolean enabled;

	/**
	 * 过滤热门城市
	 */
	private Boolean hot;
}
