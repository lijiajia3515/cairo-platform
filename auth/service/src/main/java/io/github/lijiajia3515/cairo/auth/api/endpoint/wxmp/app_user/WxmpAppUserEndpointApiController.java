package io.github.lijiajia3515.cairo.auth.api.endpoint.wxmp.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.wxmp.BindAppUserWxmpArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.wxmp.UnBindAppUserWxmpArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * [endpoint/api] appUserWxmp sns controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/wxmp_app_user")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpAppUserEndpointApiController {

	private final WxmpAppUserEndpointApiService wxmpAppUserEndpointApiService;

	/**
	 * 查询当前应用用户三方绑定列表
	 *
	 * @return MyAppUserWxmpSns list
	 *//*
	@PostMapping("/get_my_app_user_wxmp")
	public List<MyAppUserWxmp> getMyAppUserWxmp(@AuthenticationPrincipal CairoOAuthEndpointPrincipal principal) {

		String clientId = principal.getClientId();

		String userId = principal.getUserId();

		return wxmpAppUserEndpointApiService.getMyAppUserWxmp(clientId,userId);
	}*/


	/**
	 * 绑定三方应用用户
	 * @param args      args
	 */
	@PostMapping("/bind_app_user_wxmp")
	public Optional<String> bindAppUserWxmp(@Validated @RequestBody BindAppUserWxmpArgs args) {

		wxmpAppUserEndpointApiService.bindAppUserWxmp(args);

		return Optional.empty();
	}


	/**
	 * 解绑三方应用用户
	 *
	 * @param args      args
	 */
	@PostMapping("/unbind_app_user_wxmp")
	public Optional<String> unbindAppUserWxmp(@Validated @RequestBody UnBindAppUserWxmpArgs args) {

		wxmpAppUserEndpointApiService.unbindAppUserWxmp(args);

		return Optional.empty();
	}
}
