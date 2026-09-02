package io.github.lijiajia3515.cairo.auth.domain.api.client.area;

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
	 * 是否启用简称
	 */
	private boolean enableShort;
}
