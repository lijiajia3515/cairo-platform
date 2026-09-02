package io.github.lijiajia3515.cairo.auth.api.open.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant_app_user.LogoffTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant_app_user.RegisterTenantAppUserArgs;
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
 * [open/api] tenant app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/tenant_app_user")
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserOpenApiController {

	private final TenantAppUserOpenApiService tenantAppUserOpenApiService;

	/**
	 * 创建账号和用户
	 *
	 * @param args args
	 * @return 用户
	 */
	@PostMapping("/register_tenant_app_user")
	public Optional<String> registerTenantAppUser(@Validated @RequestBody RegisterTenantAppUserArgs args) {
		tenantAppUserOpenApiService.registerTenantAppUser(args);
		return Optional.empty();
	}

	/**
	 * 注销用户
	 *
	 * @param args args
	 * @return 用户
	 */
	@PostMapping("/logoff_tenant_app_user")
	public Optional<String> logoffTenantAppUser(@Validated @RequestBody LogoffTenantAppUserArgs args) {
		tenantAppUserOpenApiService.logoffTenantAppUser(args);
		return Optional.empty();
	}

}
