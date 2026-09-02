package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取菜单树参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMenuTreeArgs implements Serializable {

    /**
	 * 父级id
	 */
	private String parentId;
}
