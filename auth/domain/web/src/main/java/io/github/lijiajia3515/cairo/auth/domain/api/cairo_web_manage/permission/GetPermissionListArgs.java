package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改菜单参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetPermissionListArgs implements Serializable {

    /**
	 * 菜单id
	 */
	private List<String> menuIds;
}
