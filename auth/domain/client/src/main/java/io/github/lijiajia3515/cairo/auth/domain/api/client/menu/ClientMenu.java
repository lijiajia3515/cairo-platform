package io.github.lijiajia3515.cairo.auth.domain.api.client.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ClientMenu implements Serializable {

	/**
	 * 菜单ID
	 */
	private String menuId;

	/**
	 * 上级ID
	 */
	private String parentId;

	/**
	 * 菜单名称
	 */
	private String menuName;

	/**
	 * 前端路径/页面地址外部地址
	 */
	private String path;

	/**
	 * 组件名
	 */
	private String component;

	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 是否隐藏
	 */
	private boolean hiddenMenu;


	/**
	 * 是否隐藏
	 */
	private List<String> tags;

	/**
	 * 左值
	 */
	private int leftNo;

	/**
	 * 右值
	 */
	@Field(write = Field.Write.ALWAYS)
	private int rightNo;

	/**
	 * 深度
	 */
	@Field(write = Field.Write.ALWAYS)
	private int depth;

}
