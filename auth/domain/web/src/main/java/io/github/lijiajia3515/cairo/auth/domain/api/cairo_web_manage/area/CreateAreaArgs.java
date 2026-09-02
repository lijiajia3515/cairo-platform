package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 添加区域参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAreaArgs implements Serializable {

	/**
	 * 父级区域ID（默认跟根节点）
	 */
	private String parentAreaId;

	/**
	 * 区域ID
	 */
	@NotNull
	@NotBlank
	private String areaId;

	/**
	 * 区域名称
	 */
	@NotNull
	@NotBlank
	private String areaName;

	/**
	 * 简称
	 */
	@NotNull
	@NotBlank
	private String shortAreaName;

	/**
	 * 拼音
	 */
	@NotNull
	@NotBlank
	private String pinYin;

	/**
	 * 拼音首字母
	 */
	@NotNull
	@NotBlank
	private String pinYinPrefix;

	/**
	 * 热门
	 */
	private boolean hot;

	/**
	 * 状态
	 */
	@Builder.Default
	private boolean enabled = true;

}
