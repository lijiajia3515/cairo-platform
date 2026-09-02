package io.github.lijiajia3515.cairo.auth.domain.api.client.area;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取区域树参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAreaTreeArgs {
	/**
	 * 上级区域ID
	 */
	private String parentAreaId;
}
