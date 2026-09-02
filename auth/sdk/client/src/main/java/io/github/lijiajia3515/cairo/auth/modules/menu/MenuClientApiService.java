package io.github.lijiajia3515.cairo.auth.modules.menu;


import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;

import java.util.List;


public interface MenuClientApiService {

	/**
	 * 获取菜单树
	 *
	 * @param args      参数
	 * @return 树节点
	 */
	List<MenuNode> getMenuTreeList(GetMenuTreeArgs args);


	/**
	 * 获取菜单list
	 * @param args      参数
	 * @return menu list
	 */
	List<ClientMenu> getMenuList(GetMenuListArgs args);
}
