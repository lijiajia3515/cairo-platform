package io.github.lijiajia3515.cairo.auth.api.app_user.account;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account.ModifyMyAccountAvatarUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account.ModifyMyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account.ModifyMyAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.account.ModifyMyAccountUsernameArgs;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
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

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * [endpoint/api] account controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app_user_api/account")
@CairoSecurity(type = CairoSecurityType.APP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountAppUserApiController {
	private final AccountAppUserApiService accountAppUserApiService;

	/**
	 * 修改账号密码
	 *
	 * @param principal principal
	 * @param args      args
	 * @return empty
	 */
	@PostMapping("/modify_my_account_username")
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyMyAccountUsername(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountUsernameArgs args) {
		accountAppUserApiService.modifyMyAccountUsername(principal.getAccountId(),  args);
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
	public Optional<String> modifyMyAccountPhoneNumber(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountPhoneNumberArgs args) {
		accountAppUserApiService.modifyMyAccountPhoneNumber(principal.getAccountId(), args);
		return Optional.empty();
	}

	/**
	 * 获取当前密码状态
	 *
	 * @return 是否设置密码
	 */
	@PostMapping({"/get_my_account_password_status"})
	@PreAuthorize("isAuthenticated()")
	public Boolean getMyAccountPasswordStatus(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal) {
		return accountAppUserApiService.getMyAccountPasswordStatus(principal.getAccountId());
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
	public Optional<String> modifyMyAccountPassword(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, @Validated @RequestBody ModifyMyAccountPasswordArgs args) {
		accountAppUserApiService.modifyMyAccountPassword(principal.getAccountId(), args);
		return Optional.empty();
	}

	/**
	 * 修改账号头像
	 *
	 * @param principal 1
	 * @param request   1
	 * @return 1
	 * @throws IOException 1
	 */
	@PostMapping(value = "/modify_my_account_avatar", consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE})
	@PreAuthorize("isAuthenticated()")
	public Optional<String> modifyAccountAvatar(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal, HttpServletRequest request) throws IOException {
		accountAppUserApiService.modifyMyAccountAvatar(principal.getAccountId(), request.getInputStream(), request.getContentType(), request.getContentLengthLong());
		return Optional.empty();
	}
}
