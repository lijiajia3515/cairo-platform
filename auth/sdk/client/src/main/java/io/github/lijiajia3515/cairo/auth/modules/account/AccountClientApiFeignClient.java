package io.github.lijiajia3515.cairo.auth.modules.account;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
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
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Optional;

/**
 * client-api-account feignclient
 */
@FeignClient(
	contextId = "accountClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/account",
	fallbackFactory = AccountClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AccountClientApiFeignClient {

	/**
	 * 获取单个账号
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_info")
	ResponseEntity<BusinessResult<Account>> getAccountInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
														   @RequestBody GetAccountInfoArgs args);

	/**
	 * 获取账号列表
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号 page
	 */
	@PostMapping("/get_account_list")
	ResponseEntity<BusinessResult<List<Account>>> getAccountList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																 @RequestBody GetAccountListArgs args);

	/**
	 * 获取账号分页列表
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号 list
	 */
	@PostMapping("/get_account_page_list")
	ResponseEntity<BusinessResult<Page<Account>>> getAccountPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																	 @RequestBody GetAccountPageListArgs args);

	/**
	 * 获取账号认证
	 * 需要权限 account:account_auth | account:all
	 *
	 * @param args args
	 * @return auth model
	 */
	@PostMapping("/get_account_auth")
	ResponseEntity<BusinessResult<CairoAccountAuthModel>> getAccountAuth(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																		 @RequestBody GetAccountAuthArgs args);


	/**
	 * 创建账号
	 * 需要权限 account:create_account | account:all
	 *
	 * @param args args
	 */
	@PostMapping("/create_account")
	ResponseEntity<BusinessResult<Optional<String>>> createAccount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																   @Validated @RequestBody CreateAccountArgs args);

	/**
	 * 修改账号用户名
	 * 需要权限 account:modify_account_username | account:all
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/modify_account_username")
	ResponseEntity<BusinessResult<Optional<String>>> modifyAccountUsername(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																		   @Validated @RequestBody ModifyAccountUsernameArgs args);


	/**
	 * 修改账号手机号
	 * 需要权限 account:modify_account_phone_number | account:all
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/modify_account_phone_number")
	ResponseEntity<BusinessResult<Optional<String>>> modifyAccountPhoneNumber(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			  @Validated @RequestBody ModifyAccountPhoneNumberArgs args);

	/**
	 * 获取账号密码状态
	 * 需要权限 account:account_password_status | account:all
	 *
	 * @return 是否设置密码
	 */
	@PostMapping({"/get_account_password_status"})
	ResponseEntity<BusinessResult<Optional<Boolean>>> getAccountPasswordStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody GetAccountPasswordStatusArgs args);

	/**
	 * 修改账号密码
	 * 需要权限 account:modify_account_password | account:all
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/modify_password")
	ResponseEntity<BusinessResult<Optional<String>>> modifyPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody ModifyAccountPasswordArgs args);

	/**
	 * 修改账号头像
	 * 需要权限 account:modify_account_avatar | account:all
	 *
	 * @return 1
	 */
	@PostMapping(value = "/modify_account_avatar")
	ResponseEntity<BusinessResult<Optional<String>>> modifyAccountAvatar(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody ModifyAccountAvatarUrlArgs args);

}
