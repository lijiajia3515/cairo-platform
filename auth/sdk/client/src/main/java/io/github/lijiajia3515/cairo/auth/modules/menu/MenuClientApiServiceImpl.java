package io.github.lijiajia3515.cairo.auth.modules.menu;

import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MenuClientApiServiceImpl implements MenuClientApiService {

	private final MenuClientApiFeignClient menuClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public MenuClientApiServiceImpl(MenuClientApiFeignClient menuClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.menuClientApiFeignClient = menuClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<MenuNode> getMenuTreeList(GetMenuTreeArgs args) {
		try {
			ResponseEntity<BusinessResult<List<MenuNode>>> menuTreeList = menuClientApiFeignClient.getMenuTreeList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(menuTreeList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("menuTreeList error", e);
			throw e;
		}
	}

	@Override
	public List<ClientMenu> getMenuList(GetMenuListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<ClientMenu>>> menuList = menuClientApiFeignClient.getMenuList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(menuList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("menuList error", e);
			throw e;
		}
	}
}
