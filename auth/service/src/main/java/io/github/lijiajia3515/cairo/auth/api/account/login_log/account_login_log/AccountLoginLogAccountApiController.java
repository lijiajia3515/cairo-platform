package io.github.lijiajia3515.cairo.auth.api.account.login_log.account_login_log;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.account.login_log.account_login_log.AccountLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.account.login_log.account_login_log.GetAccountLoginLogArgs;
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

/**
 * [account/api] account login log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/account_api/account_login_log")
@CairoSecurity(type = CairoSecurityType.ACCOUNT)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountLoginLogAccountApiController {

	private final AccountLoginLogAccountApiService accountLoginLogAccountApiService;


	/**
	 * 获取登录日志list
	 *
	 * @param principal principal
	 * @param args      args
	 * @return account login log list
	 */
	@PostMapping("/get_my_account_login_log_list")
	@PreAuthorize("isAuthenticated()")
	public List<AccountLoginLog> getMyAccountLoginLogList(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal,
														  @Validated @RequestBody(required = false) GetAccountLoginLogArgs args) {
		String appId = principal.getAppId();
		String accountId = principal.getAccountId();

		if (args == null) {
			args = new GetAccountLoginLogArgs();
		}

		return accountLoginLogAccountApiService.getMyAccountLoginLogList(accountId, appId, args);
	}

	/**
	 * 获取登录日志
	 *
	 * @param principal principal
	 * @param args      args
	 * @return user login log page list
	 */
	@PostMapping("/get_my_account_login_log_page_list")
	@PreAuthorize("isAuthenticated()")
	public Page<AccountLoginLog> getMyAccountLoginLogPageList(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal,
															  @Validated @RequestBody(required = false) GetAccountLoginLogArgs args) {
		String appId = principal.getAppId();
		String accountId = principal.getAccountId();
		if (args == null) {
			args = new GetAccountLoginLogArgs();
		}
		return accountLoginLogAccountApiService.getMyAccountLoginLogPageList(appId, accountId, args);
	}

}
