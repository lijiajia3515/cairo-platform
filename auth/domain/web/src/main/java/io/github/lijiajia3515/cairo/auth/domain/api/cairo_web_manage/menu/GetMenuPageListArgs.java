package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取菜单分页集合参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMenuPageListArgs extends AbstractPage<GetMenuPageListArgs> implements Serializable {

	/**
	 * 父级id
	 */
	private String parentId;
}
