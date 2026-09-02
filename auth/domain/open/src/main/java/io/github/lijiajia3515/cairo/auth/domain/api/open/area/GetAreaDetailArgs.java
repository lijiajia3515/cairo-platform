package io.github.lijiajia3515.cairo.auth.domain.api.open.area;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取区域详情参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAreaDetailArgs {
	/**
	 * 区域ID
	 */
	private String areaId;

	/**
	 * 开启简称
	 */
	private boolean enableShort;
}
