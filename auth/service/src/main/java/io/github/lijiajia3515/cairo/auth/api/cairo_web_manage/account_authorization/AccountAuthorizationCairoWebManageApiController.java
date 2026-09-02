package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.account_authorization;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.account_authorization.AccountAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account_authorization.GetAccountAuthorizationListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account_authorization.OfflineAccountAuthorizationArgs;
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
 * [cairo-web-manage/api] account authorization controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/account_authorization")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AccountAuthorizationCairoWebManageApiController {

	private final AccountAuthorizationCairoWebManageApiService permissionCairoWebManageApiService;

	/**
	 * 获取账号会话list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 账号会话集合
	 */
	@PostMapping("/get_account_authorization_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:read')")
	@CairoContext
	public List<AccountAuthorization> getAccountAuthorizationList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody(required = false) GetAccountAuthorizationListArgs args) {

		if (args == null) {
			args = new GetAccountAuthorizationListArgs();
		}
		return permissionCairoWebManageApiService.getAccountAuthorizationList(args);
	}

	/**
	 * 获取账号会话list page
	 *
	 * @param args      参数
	 * @return 账号会话分页集合
	 */
	@PostMapping("/get_account_authorization_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:read')")
	@CairoContext
	public Page<AccountAuthorization> getAccountAuthorizationPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody(required = false) GetAccountAuthorizationListArgs args) {
		if (args == null) {
			args = new GetAccountAuthorizationListArgs();
		}
		return permissionCairoWebManageApiService.getAccountAuthorizationPageList(args);
	}

	/**
	 * 下线账号会话
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/offline_account_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:offline')")
	@CairoContext
	public Optional<String> offlineAccountAuthorization(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody OfflineAccountAuthorizationArgs args) {
		permissionCairoWebManageApiService.offlineAccountAuthorization(args);
		return Optional.empty();
	}

	/**
	 * 下线所有账号会话
	 *
	 * @param principal 凭证
	 * @return empty
	 */
	@PostMapping("/offline_all_account_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'account_authorization:all', 'account_authorization:offline')")
	@CairoContext
	public Optional<String> offlineAllAccountAuthorization(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		permissionCairoWebManageApiService.offlineAllAccountAuthorization();
		return Optional.empty();
	}


}
