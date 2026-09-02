package io.github.lijiajia3515.cairo.auth.api.account.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.account.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [account/api] tenant app user controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/account_api/tenant_app_user")
@CairoSecurity(type = CairoSecurityType.ACCOUNT)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserAccountApiController {

	private final TenantAppUserAccountApiService tenantAccountApiService;

	/**
	 * 获取我的企业身份列表
	 *
	 * @param principal principal
	 * @return 身份列表
	 */
	@PostMapping("/get_my_tenant_app_user_list")
	@PreAuthorize("isAuthenticated()")
	@CairoContext
	public List<TenantAppUser> getMyTenantAppUserList(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal) {
		String appId = principal.getAppId();
		String accountId = principal.getAccountId();
		return tenantAccountApiService.getMyTenantAppUserList(appId, accountId);
	}


}
