package io.github.lijiajia3515.cairo.auth.domain.dto.dict.sys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PathSysDict implements Serializable {
	/**
	 * 字典
	 */
	private String dictId;

	/**
	 * 字典项id
	 */
	private List<String> itemIds;

	/**
	 * 字典项名称
	 */
	private List<String> itemNames;

	/**
	 * 备注
	 */
	private List<String> remarks;

	/**
	 * 图标值
	 */
	private List<String> icons;

}
