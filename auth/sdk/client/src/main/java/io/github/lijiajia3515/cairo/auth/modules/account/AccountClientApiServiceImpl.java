package io.github.lijiajia3515.cairo.auth.modules.account;


import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
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
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AccountClientApiServiceImpl implements AccountClientApiService {

	private final AccountClientApiFeignClient accountClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AccountClientApiServiceImpl(AccountClientApiFeignClient accountClientApiFeignClient,
									   CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.accountClientApiFeignClient = accountClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public Account getAccountInfo(GetAccountInfoArgs args) {
		try {
			ResponseEntity<BusinessResult<Account>> accountInfo = accountClientApiFeignClient.getAccountInfo(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountInfo.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAccountInfo error", e);
			throw e;
		}
	}

	@Override
	public List<Account> getAccountList(GetAccountListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Account>>> accountList = accountClientApiFeignClient.getAccountList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("getAccountList error", e);
			throw e;
		}
	}

	@Override
	public Map<String, Account> getAccountMap(GetAccountListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Account>>> accountList = accountClientApiFeignClient.getAccountList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(Account::getAccountId, x->x));
		} catch (Exception e) {
			log.error("getAccountMap error", e);
			throw e;
		}
	}

	@Override
	public Page<Account> getAccountPageList(GetAccountPageListArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<Account>>> accountPageList = accountClientApiFeignClient.getAccountPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("accountPageList error", e);
			throw e;
		}
	}

	@Override
	public CairoAccountAuthModel getAccountAuth(GetAccountAuthArgs args) {
		try {
			ResponseEntity<BusinessResult<CairoAccountAuthModel>> accountAuth = accountClientApiFeignClient.getAccountAuth(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountAuth.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAccountAuth error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> createAccount(CreateAccountArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> account = accountClientApiFeignClient.createAccount(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("createAccount error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> modifyAccountUsername(ModifyAccountUsernameArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> account = accountClientApiFeignClient.modifyAccountUsername(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("modifyAccountUsername error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> modifyAccountPhoneNumber(ModifyAccountPhoneNumberArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> account = accountClientApiFeignClient.modifyAccountPhoneNumber(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("modifyAccountPhoneNumber error", e);
			throw e;
		}
	}

	@Override
	public Optional<Boolean> getAccountPasswordStatus(GetAccountPasswordStatusArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<Boolean>>> account = accountClientApiFeignClient.getAccountPasswordStatus(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAccountPasswordStatus error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> modifyPassword(ModifyAccountPasswordArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> account = accountClientApiFeignClient.modifyPassword(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("modifyPassword error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> modifyAccountAvatar(ModifyAccountAvatarUrlArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> account = accountClientApiFeignClient.modifyAccountAvatar(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(account.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("modifyAccountAvatar error", e);
			throw e;
		}
	}
}
