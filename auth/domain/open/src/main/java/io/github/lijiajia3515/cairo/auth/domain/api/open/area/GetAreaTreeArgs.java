package io.github.lijiajia3515.cairo.auth.domain.api.open.area;

import jakarta.validation.constraints.Min;
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

	/**
	 * 深度
	 */
	@Min(1)
	@Builder.Default
	private int depth = 2;
	/**
	 * 启用简称
	 */
	private boolean enableShort;
}
