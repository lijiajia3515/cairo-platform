package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.account;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.MetadataAccount;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.CreateAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.DeleteAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.GetAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.LogoffAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountLockArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ResetAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.UnlogoffAccountArgs;
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
 * [cairo-web-manage/api] account controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/account")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountCairoWebManageApiController {
	private final AccountCairoWebManageApiService accountCairoWebManageApiService;

	/**
	 * 获取账号列表
	 *
	 * @param args args
	 * @return 账号 list
	 */
	@PostMapping("/get_account_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public List<MetadataAccount> getAccountList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody GetAccountPageListArgs args) {
		return accountCairoWebManageApiService.getAccountList(args);
	}

	/**
	 * 获取账号分页列表
	 *
	 * @param args args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:read')")
	public Page<MetadataAccount> getAccountPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody GetAccountPageListArgs args) {
		return accountCairoWebManageApiService.getAccountPageList(args);
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
	public Optional<MetadataAccount> getAccountById(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody GetAccountInfoArgs args) {
		return Optional.ofNullable(accountCairoWebManageApiService.getAccountInfo(args.getAccountId()));
	}


	/**
	 * 创建账号
	 *
	 * @param args args
	 */
	@PostMapping("/create_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:create_account')")
	public Optional<String> createAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody CreateAccountArgs args) {
		accountCairoWebManageApiService.createAccount(args);
		return Optional.empty();
	}

	/**
	 * 管理员重置用户密码
	 *
	 * @param args args
	 */
	@PostMapping("/reset_account_password")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:reset_account_password')")
	public Optional<String> resetAccountPassword(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												 @Validated @RequestBody ResetAccountPasswordArgs args) {
		accountCairoWebManageApiService.resetAccountPassword(args);
		return Optional.empty();
	}

	/**
	 * 注销账号
	 *
	 * @param args args
	 */
	@PostMapping("/logoff_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:logoff_account')")
	public Optional<String> logoffAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody LogoffAccountArgs args) {
		accountCairoWebManageApiService.logoffAccount(args);
		return Optional.empty();
	}

	/**
	 * 取消注销账号
	 *
	 * @param args args
	 */
	@PostMapping("/unlogoff_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:unlogoff_account')")
	public Optional<String> unlogoffAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody UnlogoffAccountArgs args) {
		accountCairoWebManageApiService.unlogoffAccount(args);
		return Optional.empty();
	}

	/**
	 * 删除账号
	 *
	 * @param args args
	 */
	@PostMapping("/delete_account")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:delete_account')")
	public Optional<String> deleteAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										  @Validated @RequestBody DeleteAccountArgs args) {
		accountCairoWebManageApiService.deleteAccount(args);
		return Optional.empty();
	}

	/**
	 * 修改账号信息
	 *
	 * @param args args
	 */
	@PostMapping("/modify_account_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_info')")
	public Optional<String> modifyAccountInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody ModifyAccountInfoArgs args) {
		accountCairoWebManageApiService.modifyAccountInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改账号状态
	 *
	 * @param args args
	 */
	@PostMapping("/modify_account_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_status')")
	public Optional<String> modifyAccountStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody ModifyAccountStatusArgs args) {
		accountCairoWebManageApiService.modifyAccountStatus(args);
		return Optional.empty();
	}

	/**
	 * 修改账号锁定状态
	 *
	 * @param args args
	 */
	@PostMapping("/modify_account_lock_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account:all', 'account:modify_account_lock_status')")
	public Optional<String> modifyAccountLockStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody ModifyAccountLockArgs args) {
		accountCairoWebManageApiService.modifyAccountLockStatus(args);
		return Optional.empty();
	}


}
