package io.github.lijiajia3515.cairo.auth.modules.menu;

import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class MenuClientApiFallbackFeignClient implements MenuClientApiFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<MenuNode>>> getMenuTreeList(String authorization, GetMenuTreeArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<ClientMenu>>> getMenuList(String authorization, GetMenuListArgs args) {
		throw EX;
	}
}

