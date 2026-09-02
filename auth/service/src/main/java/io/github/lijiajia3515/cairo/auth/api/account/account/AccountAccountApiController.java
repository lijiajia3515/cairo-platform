package io.github.lijiajia3515.cairo.auth.api.account.account;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.account.account.AccountPrincipalModel;
import io.github.lijiajia3515.cairo.auth.domain.api.account.account.ModifyAccountAvatarUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.account.account.ModifyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/**
 * [account/api] account controller
 */
@Validated
@RestController
@RequestMapping("/account_api/account")
@CairoSecurity(type = CairoSecurityType.ACCOUNT)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountAccountApiController {
	private final AccountAccountApiService accountAccountApiService;

	/**
	 * 获取当前账号信息
	 *
	 * @param principal 凭证
	 * @return 账号信息
	 */
	@PostMapping("/get_my_account_info")
	@PreAuthorize("isAuthenticated()")
	public AccountPrincipalModel getMyAccountInfo(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal) {
		return AccountPrincipalModel.builder()
			.id(principal.getId())
			.loginType(principal.getLoginType().getValue())
			.appId(principal.getAppId())
			.clientId(principal.getClientId())
			.accountId(principal.getAccountId())
			.avatarUrl(principal.getAvatarUrl())
			.nickname(principal.getNickname())
			.username(principal.getUsername())
			.phoneNumber(principal.getPhoneNumber())
			.email(principal.getEmail())
			.tags(principal.getTags())
			.departments(principal.getDepartments())
			.roles(principal.getRoles())
			.build();
	}

	/**
	 * 修改账号密码
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_my_account_password")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAccountPassword(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal, @Validated @RequestBody ModifyAccountPasswordArgs args) {
		accountAccountApiService.modifyMyAccountPassword(principal.getAccountId(), args);
		return Optional.empty();
	}
	/**
	 * 注销账号
	 */
	@PostMapping("/logoff_my_account")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> logoffMyAccount(@AuthenticationPrincipal CairoOAuthAccountPrincipal principal) {
		accountAccountApiService.logoffMyAccount(principal.getAccountId());
		return Optional.empty();
	}
}
