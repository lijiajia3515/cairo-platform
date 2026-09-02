package io.github.lijiajia3515.cairo.auth.api.client.account;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account.SearchAccountArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CairoAccountAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CreateAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountPasswordStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountAvatarUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountUsernameArgs;
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
 * [client/api] account service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/account")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AccountClientApiController {

	private final AccountClientApiService accountClientApiService;

	/**
	 * 获取账号信息
	 *
	 * @param principal 用户凭证才能访问
	 * @return 最新的用户凭证模型
	 */
	@PostMapping("/get_account_auth")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:account_auth')")
	public CairoAccountAuthModel getAccountAuth(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountAuthArgs args) {
		return accountClientApiService.getAccountAuth(args);
	}

	/**
	 * 查询单个账号
	 *
	 * @param principal 凭证
	 * @param args      args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_info")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:read')")
	public Optional<Account> getAccountInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountInfoArgs args) {
		return Optional.ofNullable(accountClientApiService.getAccountInfo(args));
	}

	/**
	 * 查询账号 [client调用]
	 *
	 * @param principal 凭证
	 * @param args      args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_list")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:read')")
	public List<Account> getAccountList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountListArgs args) {
		return accountClientApiService.getAccountList(args);
	}

	/**
	 * 查询账号 (分页模式)[client调用]
	 *
	 * @param principal 凭证
	 * @param args      args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_page_list")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:read')")
	public Page<Account> getAccountPageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountPageListArgs args) {
		return accountClientApiService.getAccountPageList(args);
	}

	/**
	 * 搜索账号
	 *
	 * @param args args
	 * @return 账号
	 */
	@PostMapping("/search_account_info")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:read')")
	public Account searchAccountInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
									 @Validated @RequestBody SearchAccountArgs args) {
		return accountClientApiService.searchAccountInfo(args);
	}


	/**
	 * 创建账号
	 *
	 * @param args args
	 */
	@PostMapping("/create_account")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:create_account')")
	public Optional<String> createAccount(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										  @Validated @RequestBody CreateAccountArgs args) {
		accountClientApiService.createAccount(args);
		return Optional.empty();
	}

	/**
	 * 修改账号用户名
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_account_username")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:modify_account_username')")
	public Optional<String> modifyAccountUsername(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody ModifyAccountUsernameArgs args) {
		accountClientApiService.modifyAccountUsername(args.getAccountId(), args);
		return Optional.empty();
	}


	/**
	 * 修改账号手机号
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_account_phone_number")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:modify_account_phone_number')")
	public Optional<String> modifyAccountPhoneNumber(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody ModifyAccountPhoneNumberArgs args) {
		accountClientApiService.modifyAccountPhoneNumber(args.getAccountId(), args);
		return Optional.empty();
	}

	/**
	 * 获取账号密码状态
	 *
	 * @return 是否设置密码
	 */
	@PostMapping({"/get_account_password_status"})
	@PreAuthorize("hasAnyAuthority('account:all', 'account:account_password_status')")
	public Optional<Boolean> getAccountPasswordStatus(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountPasswordStatusArgs args) {
		return Optional.of(accountClientApiService.getAccountPasswordStatus(args.getAccountId()));
	}

	/**
	 * 修改账号密码
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_password")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:modify_account_password')")
	public Optional<String> modifyPassword(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody ModifyAccountPasswordArgs args) {
		accountClientApiService.modifyPassword(args);
		return Optional.empty();
	}

	/**
	 * 修改账号头像
	 *
	 * @param principal 1
	 * @return 1
	 */
	@PostMapping(value = "/modify_account_avatar")
	@PreAuthorize("hasAnyAuthority('account:all', 'account:modify_account_avatar')")
	public Optional<String> modifyAccountAvatar(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody ModifyAccountAvatarUrlArgs args) {
		accountClientApiService.modifyAccountAvatar(args);
		return Optional.empty();
	}




}
