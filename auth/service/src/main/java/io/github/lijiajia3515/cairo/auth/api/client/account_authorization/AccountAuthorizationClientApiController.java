package io.github.lijiajia3515.cairo.auth.api.client.account_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
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

/**
 * [client/api] account authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/account_authorization")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AccountAuthorizationClientApiController {

	private final AccountAuthorizationClientApiService accountAuthorizationClientApiService;

	/**
	 * 获取账号认证
	 *
	 * @param principal 用户凭证才能访问
	 * @return 账号凭证
	 */
	@PostMapping("/get_account_authorization")
	@PreAuthorize("hasAnyAuthority('account_authorization:all', 'account_authorization:get_account_authorization')")
	public AccountAuthorizationModel getAccountAuthorization(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetAccountAuthorizationArgs args) {
		return accountAuthorizationClientApiService.getAccountAuthorization(args);
	}
}
