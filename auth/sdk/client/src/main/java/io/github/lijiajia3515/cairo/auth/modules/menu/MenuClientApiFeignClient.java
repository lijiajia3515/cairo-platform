package io.github.lijiajia3515.cairo.auth.modules.menu;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 菜单-client模式feign客户端
 */
@FeignClient(
	contextId = "menuClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/menu",
	fallbackFactory = MenuClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface MenuClientApiFeignClient {

	/**
	 * 获取菜单树
	 *
	 * @param args      参数
	 * @return 树节点
	 */
	@PostMapping("/get_menu_tree_list")
	ResponseEntity<BusinessResult<List<MenuNode>>> getMenuTreeList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetMenuTreeArgs args);


	/**
	 * 获取菜单list
	 * @param args      参数
	 * @return menu list
	 */
	@PostMapping("/get_menu_list")
	ResponseEntity<BusinessResult<List<ClientMenu>>> getMenuList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetMenuListArgs args);
}
