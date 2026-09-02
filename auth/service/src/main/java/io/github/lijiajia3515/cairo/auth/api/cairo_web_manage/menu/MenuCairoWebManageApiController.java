package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.menu;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MetadataMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.CreateMenuArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.DeleteMenuArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.ModifyMenuInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.MoveMenuArgs;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
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
import java.util.Optional;

/**
 * [cairo-web-manage/api] menu controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/menu")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class MenuCairoWebManageApiController {

	private final MenuCairoWebManageApiService menuCairoWebManageApiService;

	/**
	 * 获取菜单树形集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 树节点
	 */
	@PostMapping("/get_menu_tree_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	@CairoContext
	public List<MenuNode> getMenuList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									  @Validated @RequestBody GetMenuTreeArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		if (args == null) {
			args = new GetMenuTreeArgs();
		}
		return menuCairoWebManageApiService.getMenuTreeList(appId, endpointId, subappId, subappVersion, args);
	}

	/**
	 * 获取菜单集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return menu list
	 */
	@PostMapping("/get_menu_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	@CairoContext
	public List<MetadataMenu> getMenuList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody(required = false) GetMenuListArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		if (args == null) {
			args = new GetMenuListArgs();
		}
		return menuCairoWebManageApiService.getMenuList(appId, endpointId, subappId, subappVersion, args);
	}

	/**
	 * 获取菜单分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return menu page list
	 */
	@PostMapping("/get_menu_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:read')")
	@CairoContext
	public Page<MetadataMenu> getMenuPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody(required = false) GetMenuPageListArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		if (args == null) {
			args = new GetMenuPageListArgs();
		}
		return menuCairoWebManageApiService.getMenuPageList(appId, endpointId, subappId, subappVersion, args);
	}

	/**
	 * 创建菜单
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/create_menu")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:write')")
	@CairoContext
	public Optional<String> createMenu(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody CreateMenuArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		menuCairoWebManageApiService.createMenu(appId, endpointId, subappId, subappVersion, args);

		return Optional.empty();
	}

	/**
	 * 修改菜单信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_menu")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:write')")
	@CairoContext
	public Optional<Object> modifyMenu(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody ModifyMenuInfoArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		menuCairoWebManageApiService.modifyMenu(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}

	/**
	 * 移动菜单
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/move_menu")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:write')")
	@CairoContext
	public Optional<Object> moveMenu(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									 @Validated @RequestBody MoveMenuArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		menuCairoWebManageApiService.moveMenu(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}

	/**
	 * 删除菜单
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/delete_menu")
	@PreAuthorize("hasAnyAuthority('app_admin', 'menu:all', 'menu:write')")
	@CairoContext
	public Optional<Object> deleteMenu(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
									   @Validated @RequestBody DeleteMenuArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		menuCairoWebManageApiService.deleteMenu(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}


}
