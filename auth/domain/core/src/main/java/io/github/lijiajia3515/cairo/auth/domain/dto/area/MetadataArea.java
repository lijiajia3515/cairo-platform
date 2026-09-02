package io.github.lijiajia3515.cairo.auth.domain.dto.area;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 区域
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MetadataArea implements Serializable {
	/**
	 * 上级区域ID
	 */
	private String parentAreaId;

	/**
	 * 区域ID
	 */
	private String areaId;

	/**
	 * 区域名称
	 */
	private String areaName;

	/**
	 * 简称
	 */
	private String shortAreaName;

	/**
	 * 拼音
	 */
	private String pinYin;

	/**
	 * 拼音前缀
	 */
	private String pinYinPrefix;

	/**
	 * 层级
	 */
	private Integer depth;

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
