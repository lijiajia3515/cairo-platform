package io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 获取菜单集合参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMenuListArgs implements Serializable {

    /**
	 * 父级ID
	 */
	private String parentId;
}
