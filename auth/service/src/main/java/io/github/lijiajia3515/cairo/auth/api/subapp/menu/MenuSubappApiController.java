package io.github.lijiajia3515.cairo.auth.api.subapp.menu;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.Menu;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [subapp_user/api] menu controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/menu")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class MenuSubappApiController {

	private final MenuSubappApiService menuSubappApiService;

	/**
	 * 获取菜单树
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 树节点
	 */
	@PostMapping("/get_menu_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	public List<MenuNode> getMenuTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @RequestBody GetMenuTreeArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		if (args == null) {
			args = new GetMenuTreeArgs();
		}
		return menuSubappApiService.getMenuTreeList(appId, endpointId, principal.getSubappId(), principal.getSubappVersion(), args);
	}

	/**
	 * 获取菜单list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return menu list
	 */
	@PostMapping("/get_menu_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	public List<Menu> getMenuTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody(required = false) GetMenuListArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		if (args == null) {
			args = new GetMenuListArgs();
		}
		return menuSubappApiService.getMenuList(appId, endpointId, principal.getSubappId(), principal.getSubappVersion(), args);
	}

	/**
	 * 获取菜单list page
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return menu page list
	 */
	@PostMapping("/get_menu_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	public Page<Menu> getMenuPage(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody(required = false) GetMenuPageListArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		if (args == null) {
			args = new GetMenuPageListArgs();
		}
		return menuSubappApiService.getMenuPageList(appId, endpointId, principal.getSubappId(), principal.getSubappVersion(), args);
	}

}
