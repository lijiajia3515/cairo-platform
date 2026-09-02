package io.github.lijiajia3515.cairo.auth.api.client.account_sns;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.AccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.UnBindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsMapArgs;
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
import java.util.Map;
import java.util.Optional;

/**
 * [client/api] user connect controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/account_sns")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AccountSnsClientApiController {

	private final AccountSnsClientApiService accountSnsClientApiService;

	/**
	 * 查询账号的三方账号 openId Map
	 *
	 * @param principal principal
	 * @param args      args
	 * @return cairo connect
	 */
	@PostMapping("/get_account_sns_map")
	@PreAuthorize("hasAnyAuthority('account_sns:all', 'account_sns:read')")
	public Map<String, SnsInfo> getAccountSnsMap(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountSnsMapArgs args) {
		return accountSnsClientApiService.getAccountSnsMap(args);
	}


	/**
	 * 查询账号三方绑定列表
	 *
	 * @return AccountSns list
	 */
	@PostMapping("/get_account_sns_list")
	@PreAuthorize("hasAnyAuthority('account_sns:all', 'account_sns:read')")
	public List<AccountSns> getAccountSnsList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
											  @Validated @RequestBody GetAccountSnsArgs args) {

		String appId = principal.getAppId();

		String accountId = args.getAccountId();

		return accountSnsClientApiService.getAccountSnsList(appId, accountId, args);
	}


	/**
	 * 绑定三方账号
	 *
	 * @param args args
	 */
	@PostMapping("/bind_account_sns")
	@PreAuthorize("hasAnyAuthority('account_sns:all', 'account_sns:bind')")
	public Optional<String> bindAccountSns(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
										   @Validated @RequestBody BindAccountSnsArgs args) {
		accountSnsClientApiService.bindAccountSns(args.getAccountId(), args.getSnsToken());
		return Optional.empty();
	}


	/**
	 * 解绑三方账号
	 *
	 * @param args args
	 */
	@PostMapping("/unbind_account_sns")
	@PreAuthorize("hasAnyAuthority('account_sns:all', 'account_sns:unbind')")
	public Optional<String> unbindAccountSns(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
											 @Validated @RequestBody UnBindAccountSnsArgs args) {
		accountSnsClientApiService.unbindAccountSns(args.getAccountId(), args.getSnsPartnerId());
		return Optional.empty();
	}
}
