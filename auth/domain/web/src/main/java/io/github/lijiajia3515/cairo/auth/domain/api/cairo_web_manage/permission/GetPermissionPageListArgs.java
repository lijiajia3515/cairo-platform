package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改菜单参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetPermissionPageListArgs extends AbstractPage<GetPermissionPageListArgs> implements Serializable {

	/**
	 * 菜单id
	 */
	private List<String> menuIds;
}
