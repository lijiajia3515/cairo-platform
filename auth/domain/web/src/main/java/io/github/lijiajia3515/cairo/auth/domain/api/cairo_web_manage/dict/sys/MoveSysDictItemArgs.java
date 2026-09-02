package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys;

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
public class MoveSysDictItemArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;


	/**
	 * 移动字典项ID
	 */
	@NotNull
	private String moveItemId;

	/**
	 * 移动到的父级字典项ID
	 */
	@NotNull
	private String parentItemId;

	/**
	 * 移动到前面的字典项ID
	 */
	private String beforeItemId;

}
