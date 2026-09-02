package io.github.lijiajia3515.cairo.auth.modules.account;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountClientApiService {

	/**
	 * 获取单个账号
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号 page
	 */
	Account getAccountInfo(GetAccountInfoArgs args);

	/**
	 * 获取账号列表
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号列表
	 */
	List<Account> getAccountList(GetAccountListArgs args);

	/**
	 * 获取账号列表map
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号列表
	 */
	Map<String,Account> getAccountMap(GetAccountListArgs args);

	/**
	 * 获取账号分页列表
	 * 需要权限 account:read | account:all
	 *
	 * @param args args
	 * @return 账号 list
	 */
	Page<Account> getAccountPageList(GetAccountPageListArgs args);

	/**
	 * 获取账号认证
	 * 需要权限 account:account_auth | account:all
	 *
	 * @param args args
	 * @return auth model
	 */
	CairoAccountAuthModel getAccountAuth(GetAccountAuthArgs args);

	/**
	 * 创建账号
	 * 需要权限 account:create_account | account:all
	 * @param args args
	 */
	Optional<String> createAccount(CreateAccountArgs args);

	/**
	 * 修改账号用户名
	 * 需要权限 account:modify_account_username | account:all
	 * @param args args
	 * @return empty
	 */
	Optional<String> modifyAccountUsername(ModifyAccountUsernameArgs args);

	/**
	 * 修改账号手机号
	 * 需要权限 account:modify_account_phone_number | account:all
	 * @param args args
	 * @return empty
	 */
	Optional<String> modifyAccountPhoneNumber(ModifyAccountPhoneNumberArgs args);

	/**
	 * 获取账号密码状态
	 * @return 是否设置密码
	 * 需要权限 account:account_password_status | account:all
	 */
	Optional<Boolean> getAccountPasswordStatus(GetAccountPasswordStatusArgs args);

	/**
	 * 修改账号密码
	 * 需要权限 account:modify_account_password | account:all
	 *
	 * @param args args
	 * @return empty
	 */
	Optional<String> modifyPassword(ModifyAccountPasswordArgs args);

	/**
	 * 修改账号头像
	 * 需要权限 account:modify_account_avatar | account:all
	 * @return 1
	 */
	Optional<String> modifyAccountAvatar(ModifyAccountAvatarUrlArgs args);

}
