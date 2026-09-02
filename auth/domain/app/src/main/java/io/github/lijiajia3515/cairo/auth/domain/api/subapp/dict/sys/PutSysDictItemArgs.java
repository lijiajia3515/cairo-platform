package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 字典 刷新 值
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PutSysDictItemArgs implements Serializable {


	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 父级字典项ID
	 */
	private String parentItemId;

	/**
	 * 前字典项ID
	 */
	private String beforeItemId;
	/**
	 * 字典项ID
	 */
	private String itemId;

	/**
	 * 字典项名称
	 */
	private String itemName;

	/**
	 * 允许编辑
	 */
	private Boolean editable;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 图标值
	 */
	private String icon;

}
