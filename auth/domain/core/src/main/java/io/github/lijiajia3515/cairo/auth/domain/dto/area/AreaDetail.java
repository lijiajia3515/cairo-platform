package io.github.lijiajia3515.cairo.auth.domain.dto.area;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 区域详情
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AreaDetail implements Serializable {
	/**
	 * 区域ID
	 */
	private String areaId;
	/**
	 * 层级
	 */
	private Integer depth;

	/**
	 * 省份ID
	 */
	private String provinceId;
	/**
	 * 省份名称
	 */
	private String provinceName;
	/**
	 * 城市ID
	 */
	private String cityId;
	/**
	 * 城市名称
	 */
	private String cityName;

	/**
	 * 行政区ID
	 */
	private String districtId;
	/**
	 * 行政区名称
	 */
	private String districtName;

	/**
	 * 街道ID
	 */
	private String streetId;
	/**
	 * 街道名称
	 */
	private String streetName;

	/**
	 * 热门
	 */
	private boolean hot;

	/**
	 * 状态
	 */
	private boolean enabled;
}
