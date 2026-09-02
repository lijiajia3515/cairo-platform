package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除菜单参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteMenuArgs implements Serializable {

	/**
	 * 菜单id
	 */
	@NotNull
	private String menuId;
}
