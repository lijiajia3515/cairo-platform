package io.github.lijiajia3515.cairo.auth.api.tenant_endpoint.auth_code;

import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeModel;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.auth_code.VerifyPasswordAuthCodeArgs;
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

import jakarta.servlet.http.HttpServletRequest;

/**
 * [tenant_app_user/api] auth code controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/auth_code")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AuthCodeTenantEndpointApiController {
	private final AuthCodeTenantEndpointApiService authCodeTenantEndpointApiService;

	/**
	 * 根据账号密码获取认证凭证
	 *
	 * @param userPrincipal principal
	 * @param args          args
	 * @param request       request
	 * @return auth code token
	 */
	@PostMapping("/get_auth_code_by_verify_password")
	@PreAuthorize("isAuthenticated()")
	public AuthCodeModel getAuthCodeByVerifyPassword(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal userPrincipal,
													 @Validated(VerifyPasswordAuthCodeArgs.WebApi.class) @RequestBody VerifyPasswordAuthCodeArgs args,
													 HttpServletRequest request) {
		String accountId = userPrincipal.getAccountId();
		String tenantId = userPrincipal.getTenantId();
		String appId = userPrincipal.getAppId();
		String ip = JakartaServletUtil.getClientIP(request);
		args.setIp(ip);
		return authCodeTenantEndpointApiService.verifyPassword(accountId, tenantId, appId, args);
	}
}
