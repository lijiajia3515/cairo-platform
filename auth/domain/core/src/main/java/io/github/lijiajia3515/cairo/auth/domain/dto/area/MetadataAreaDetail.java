package io.github.lijiajia3515.cairo.auth.domain.dto.area;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
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
public class MetadataAreaDetail implements Serializable {
	/**
	 * 区域ID
	 */
	private String areaId;
	/**
	 * 层级
	 */
	private Integer depth;

	/**
	 * 拼音
	 */
	private String pinYin;

	/**
	 * 拼音前缀
	 */
	private String pinYinPrefix;

	/**
	 * 省份ID
	 */
	private String provinceId;
	/**
	 * 省份名称
	 */
	private String provinceName;
	/**
	 * 省份简称
	 */
	private String provinceShortName;

	/**
	 * 城市ID
	 */
	private String cityId;
	/**
	 * 城市名称
	 */
	private String cityName;
	/**
	 * 城市简称
	 */
	private String cityShortName;

	/**
	 * 行政区ID
	 */
	private String districtId;
	/**
	 * 行政区名称
	 */
	private String districtName;
	/**
	 * 行政区简称
	 */
	private String districtShortName;

	/**
	 * 街道ID
	 */
	private String streetId;
	/**
	 * 街道名称
	 */
	private String streetName;
	/**
	 * 街道简称
	 */
	private String streetShortName;

	/**
	 * 热门
	 */
	private boolean hot;
	/**
	 * 状态
	 */
	private boolean enabled;

	/**
	 * 排序值
	 */
	private int sort;

	/**
	 * 源信息
	 */
	private CairoAppUserMetadata metadata;
}
