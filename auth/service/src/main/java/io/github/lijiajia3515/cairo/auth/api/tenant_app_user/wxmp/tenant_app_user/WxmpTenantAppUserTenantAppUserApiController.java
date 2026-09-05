package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.wxmp.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.wxmp.user.BindTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.wxmp.user.UnBindTenantAppUserArgs;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/tenant_app_user_wxmp")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpTenantAppUserTenantAppUserApiController {

	private final WxmpTenantAppUserTenantAppUserApiService wxmpTenantAppUserTenantAppUserApiService;
	

	/**
	 * 绑定三方企业应用级用户
	 *
	 * @param args args
	 */
	@PostMapping("/bind_tenant_app_user")
	public Optional<String> bindTenantAppUserSns(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody BindTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		wxmpTenantAppUserTenantAppUserApiService.bindTenantAppUser(tenantId,args);
		return Optional.empty();
	}


	/**
	 * 解绑三方企业应用级用户
	 *
	 * @param args args
	 */
	@PostMapping("/unbind_tenant_app_user")
	public Optional<String> unbindTenantAppUserSns(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,@Validated @RequestBody UnBindTenantAppUserArgs args) {
		String tenantId = principal.getTenantId();
		wxmpTenantAppUserTenantAppUserApiService.unbindTenantAppUser(tenantId,args);
		return Optional.empty();
	}
}
