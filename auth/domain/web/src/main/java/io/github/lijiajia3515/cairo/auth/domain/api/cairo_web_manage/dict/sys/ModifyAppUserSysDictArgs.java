package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * dict save request
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppUserSysDictArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * name
	 */
	@NotNull
	private String dictName;


	/**
	 * 字典类型
	 */
	private String dictType;

	/**
	 * 是否允许添加子项
	 */
	private Boolean isCreateItem;


}
