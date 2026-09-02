package io.github.lijiajia3515.cairo.auth.api.account.biz_log.account_biz_log;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.account.biz_log.account_biz_log.GetAccountBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.account.biz_log.account_biz_log.MyAccountBizLog;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
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

/**
 * [account/api] account biz log controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/account_api/account_biz_log")
@CairoSecurity(type = CairoSecurityType.ACCOUNT)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountBizLogAccountApiController {

	private final AccountBizLogAccountApiService accountBizLogAccountApiService;

	/**
	 * 获取我的账号业务日志
	 *
	 * @param principal principal
	 * @param args      args
	 * @return my account biz log page list
	 */
	@PostMapping("/get_my_account_biz_log_page_list")
	public Page<MyAccountBizLog> getMyAccountBizLogPageList(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal,
                                                            @Validated @RequestBody(required = false) GetAccountBizLogArgs args) {
		String accountId = principal.getAccountId();
		return accountBizLogAccountApiService.getMyAccountBizLogPageList(accountId, Optional.ofNullable(args).orElse(GetAccountBizLogArgs.builder().build()));
	}
}
