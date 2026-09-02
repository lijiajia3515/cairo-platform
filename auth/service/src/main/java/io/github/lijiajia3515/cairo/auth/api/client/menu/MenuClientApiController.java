package io.github.lijiajia3515.cairo.auth.api.client.menu;


import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/menu")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
public class MenuClientApiController {

	private final MenuClientApiService menuClientApiService;

	public MenuClientApiController(MenuClientApiService menuClientApiService) {
		this.menuClientApiService = menuClientApiService;
	}

	/**
	 * 获取菜单树
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 树节点
	 */
	@PostMapping("/get_menu_tree_list")
	@PreAuthorize("hasAnyAuthority('menu:all', 'menu:read')")
	public List<MenuNode> getMenuTreeList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody @Validated GetMenuTreeArgs args) {
		String appId = principal.getAppId();
		return menuClientApiService.getMenuTreeList(appId,  args.getEndpointId(),args.getSubappId(),args.getSubappVersion(),args.getParentId());
	}

	/**
	 * 获取菜单list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return menu list
	 */
	@PostMapping("/get_menu_list")
	@PreAuthorize("hasAnyAuthority('menu:all', 'menu:read')")
	public List<ClientMenu> getMenuList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody(required = false) GetMenuListArgs args) {
		String appId = principal.getAppId();
		String endpointId = args.getEndpointId();
		String subappId = args.getSubappId();
		String subappVersion = args.getSubappVersion();
		return menuClientApiService.getMenuList(appId, endpointId,subappId,subappVersion, args);
	}
}
