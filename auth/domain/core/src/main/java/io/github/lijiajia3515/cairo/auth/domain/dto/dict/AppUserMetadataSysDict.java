package io.github.lijiajia3515.cairo.auth.domain.dto.dict;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 字典
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUserMetadataSysDict implements Serializable {

	/**
	 * 字典ID
	 */
	private String dictId;

	/**
	 * 字典名称
	 */
	private String dictName;

	/**
	 * 字典类型
	 */
	private String dictType;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


	/**
	 * 排序值
	 */
	private Number sort;

	/**
	 * values
	 */
	private List<AppUserMetadataSysDictItem> items;


	/**
	 * 是否允许添加子项
	 */
	private Boolean isCreateItem;

	/**
	 * metadata
	 */
	private CairoAppUserMetadata metadata;

}
