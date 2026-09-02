package io.github.lijiajia3515.cairo.auth.domain.api.subapp.dict.sys;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 创建字典参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateSysDictArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 字典类型
	 */
	private String dictType;

	/**
	 * 字典名称
	 */
	@NotNull
	private String dictName;

	/**
	 * icon
	 */
	private String icon;

	/**
	 * 启用状态
	 */
	private Boolean enabled;


	/**
	 * 是否允许添加子项
	 */
	private Boolean isCreateItem;
}
