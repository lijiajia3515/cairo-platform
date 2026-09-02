package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.account;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.account.GetAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.SearchAccountArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
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
 * [tenant_subapp_user/api] account controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/account")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountTenantSubappApiController {
	private final AccountTenantSubappApiService accountTenantAppUserApiService;

	/**
	 * 获取账号列表
	 *
	 * @param args args
	 * @return 账号 list
	 */
	@PostMapping("/get_account_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public List<Account> getAccountList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
										@Validated @RequestBody GetAccountPageListArgs args) {
		return accountTenantAppUserApiService.getAccountList(args);
	}

	/**
	 * 获取账号分页列表
	 *
	 * @param args args
	 * @return 账号 page list
	 */
	@PostMapping("/get_account_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public Page<Account> getAccountPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											@Validated @RequestBody GetAccountPageListArgs args) {
		return accountTenantAppUserApiService.getAccountPageList(args);
	}

	/**
	 * 搜索账号
	 *
	 * @param args args
	 * @return 账号 page list
	 */
	@PostMapping("/search_account_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public Account searchAccountInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
									 @Validated @RequestBody SearchAccountArgs args) {
		return accountTenantAppUserApiService.searchAccountInfo(args);
	}

	/**
	 * 根据账号id获取账号信息
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping({"/get_account_info"})
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public Optional<Account> getAccountById(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
											@Validated @RequestBody GetAccountInfoArgs args) {
		return Optional.ofNullable(accountTenantAppUserApiService.getAccountInfo(args.getAccountId()));
	}

}
