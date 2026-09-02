package io.github.lijiajia3515.cairo.auth.api.open.app_user;


import io.github.lijiajia3515.cairo.auth.domain.api.open.app_user.LogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_user.RegisterAppUserArgs;
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
 * [open/api] app_user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/app_user")
@RequiredArgsConstructor
@BusinessResultBody
public class AppUserOpenApiController {

	private final AppUserOpenApiService userOpenApiService;

	/**
	 * 创建账号和用户
	 *
	 * @param args args
	 * @return 用户
	 */
	@PostMapping("/register_app_user")
	public Optional<String> registerAppUser(@Validated @RequestBody RegisterAppUserArgs args) {
		userOpenApiService.registerAppUser(args);
		return Optional.empty();
	}

	/**
	 * 注销用户
	 *
	 * @param args args
	 * @return 用户
	 */
	@PostMapping("/logoff_app_user")
	public Optional<String> logoffAppUser(@Validated @RequestBody LogoffAppUserArgs args) {
		userOpenApiService.logoffAppUser(args);
		return Optional.empty();
	}

}
