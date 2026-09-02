package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.dict.sys;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * dict 修改图标
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppUserSysDictIconArgs implements Serializable {

	/**
	 * 字典ID
	 */
	@NotNull
	private String dictId;

	/**
	 * 图标
	 */
	@NotNull
	private String icon;


}
