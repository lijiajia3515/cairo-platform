package io.github.lijiajia3515.cairo.auth.api.tenant_endpoint.account;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.ModifyMyAccountAvatarUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.ModifyMyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.ModifyMyAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.account.ModifyMyAccountUsernameArgs;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * [tenant_endpoint/api] account controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_app_user_api/account")
@CairoSecurity(type = CairoSecurityType.TENANT_APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountTenantEndpointApiController {
	private final AccountTenantEndpointApiService accountTenantEndpointApiService;

	/**
	 * 获取当前密码状态
	 *
	 * @return 是否设置密码
	 */
	@PostMapping({"/get_my_account_password_status"})
	@PreAuthorize("isAuthenticated()")
	public Boolean getMyAccountPasswordStatus(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal) {
		String accountId = principal.getAccountId();
		return accountTenantEndpointApiService.getMyAccountPasswordStatus(accountId);
	}

	/**
	 * 修改当前用户密码
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping({"/modify_my_account_password"})
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAccountPassword(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountPasswordArgs args) {
		String accountId = principal.getAccountId();
		accountTenantEndpointApiService.modifyMyAccountPassword(accountId, args);
		return Optional.empty();
	}

	/**
	 * 修改账号头像
	 *
	 * @param principal 1
	 * @param request   1
	 * @return 1
	 * @throws java.io.IOException 1
	 */
	@PostMapping(value = "/modify_my_account_avatar", consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE})
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyAccountAvatar(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, HttpServletRequest request) throws IOException {
		accountTenantEndpointApiService.modifyMyAccountAvatar(principal.getAccountId(), request.getInputStream(), request.getContentType(), request.getContentLengthLong());
		return Optional.empty();
	}


	/**
	 * 修改账号手机号
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_my_account_phone_number")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAccountPhoneNumber(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountPhoneNumberArgs args) {
		accountTenantEndpointApiService.modifyMyAccountPhoneNumber(principal.getAccountId(), args);
		return Optional.empty();
	}

	/**
	 * 修改账号密码
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_my_account_username")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAccountUsername(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountUsernameArgs args) {
		accountTenantEndpointApiService.modifyMyAccountUsername(principal.getAccountId(), args);
		return Optional.empty();
	}
}
