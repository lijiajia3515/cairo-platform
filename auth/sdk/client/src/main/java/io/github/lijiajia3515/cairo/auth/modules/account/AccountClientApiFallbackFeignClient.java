package io.github.lijiajia3515.cairo.auth.modules.account;

import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
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
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

/**
 * client-api-account fallback feignclient
 */
public class AccountClientApiFallbackFeignClient implements AccountClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-账号子应用故障");

	@Override
	public ResponseEntity<BusinessResult<Account>> getAccountInfo(String authorization, GetAccountInfoArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<Account>>> getAccountList(String authorization, GetAccountListArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<Account>>> getAccountPageList(String authorization, GetAccountPageListArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<CairoAccountAuthModel>> getAccountAuth(String authorization, GetAccountAuthArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> createAccount(String authorization, CreateAccountArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> modifyAccountUsername(String authorization, ModifyAccountUsernameArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> modifyAccountPhoneNumber(String authorization, ModifyAccountPhoneNumberArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<Boolean>>> getAccountPasswordStatus(String authorization, GetAccountPasswordStatusArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> modifyPassword(String authorization, ModifyAccountPasswordArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> modifyAccountAvatar(String authorization, ModifyAccountAvatarUrlArgs args) {
		throw EX;
	}
}
