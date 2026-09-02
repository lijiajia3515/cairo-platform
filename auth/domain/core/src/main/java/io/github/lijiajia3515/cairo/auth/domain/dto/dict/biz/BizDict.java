package io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用字典
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BizDict implements Serializable {

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
	 * items
	 */
	private List<BizDictItem> items;

}
