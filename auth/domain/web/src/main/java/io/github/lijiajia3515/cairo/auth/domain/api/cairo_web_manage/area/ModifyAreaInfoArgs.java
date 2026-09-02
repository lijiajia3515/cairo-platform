package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.area;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyAreaInfoArgs implements Serializable {
	/**
	 * 区域ID
	 */
	@NotNull
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
	 * 拼音首字母
	 */
	private String pinYinPrefix;

	/**
	 * 排序值
	 */
	private long sort;

}
