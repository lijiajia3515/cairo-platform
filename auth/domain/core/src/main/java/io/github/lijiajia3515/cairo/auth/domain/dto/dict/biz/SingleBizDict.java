package io.github.lijiajia3515.cairo.auth.domain.dto.dict.biz;

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
public class SingleBizDict implements Serializable {
	/**
	 * 字典id
	 */
	private String dictId;

	/**
	 * 字典项
	 */
	private List<Item> items;

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@SuperBuilder(toBuilder = true)
	public static class Item implements Serializable {
		/**
		 * 字典项id
		 */
		private String itemId;

		/**
		 * 字典项名称
		 */
		private String itemName;

		/**
		 * 备注
		 */
		private String remark;

		/**
		 * 图标值
		 */
		private String icon;

	}
}
