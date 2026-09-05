package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.account_sns;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account_sns.GetMyAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account_sns.MyAccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account_sns.UnBindAccountSnsArgs;
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
 * [tenant_endpoint/api] account sns controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/account_sns")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class AccountSnsTenantAppUserApiController {

	private final AccountSnsTenantAppUserApiService accountSnsTenantAppUserApiService;

	/**
	 * 查询当前账号三方绑定列表
	 *
	 * @return AccountSns list
	 */
	@PostMapping("/get_my_account_sns_list")
	@PreAuthorize("isAuthenticated()")
	public List<MyAccountSns> getMyAccountSnsList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody GetMyAccountSnsArgs args) {
		String appId = principal.getAppId();
		String accountId = principal.getAccountId();
		return accountSnsTenantAppUserApiService.getMyAccountSnsList(appId, accountId, args);
	}


	/**
	 * 绑定三方账号
	 *
	 * @param args args
	 */
	@PostMapping("/bind_account_sns")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> bindAccountSns(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody BindAccountSnsArgs args) {

		accountSnsTenantAppUserApiService.bindAccountSns(principal.getAccountId(), args.getSnsToken());

		return Optional.empty();
	}


	/**
	 * 解绑三方账号
	 *
	 * @param args args
	 */
	@PostMapping("/unbind_account_sns")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> unbindAccountSns(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody UnBindAccountSnsArgs args) {

		accountSnsTenantAppUserApiService.unbindAccountSns(principal.getAccountId(), args.getSnsPartnerId());

		return Optional.empty();
	}
}
