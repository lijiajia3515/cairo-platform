package io.github.lijiajia3515.cairo.auth.domain.api.client.area;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获取区域详情参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAreaDetailMapArgs {
	/**
	 * 区域ID
	 */
	private List<String> areaIds;

	/**
	 * 是否启用简称
	 */
	private boolean enableShort;
}
