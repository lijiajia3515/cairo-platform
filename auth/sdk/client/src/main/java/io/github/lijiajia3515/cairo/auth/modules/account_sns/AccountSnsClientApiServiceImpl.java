package io.github.lijiajia3515.cairo.auth.modules.account_sns;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.AccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.UnBindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AccountSnsClientApiServiceImpl implements AccountSnsClientApiService {

	private final AccountSnsClientApiFeignClient accountSnsClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AccountSnsClientApiServiceImpl(AccountSnsClientApiFeignClient accountSnsClientApiFeignClient,
										  CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.accountSnsClientApiFeignClient = accountSnsClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public Map<String, SnsToken> getAccountSnsMap(GetAccountSnsMapArgs args) {
		try {
			ResponseEntity<BusinessResult<Map<String, SnsToken>>> accountSnsMap = accountSnsClientApiFeignClient.getAccountSnsMap(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountSnsMap.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("accountSnsMap error", e);
			throw e;
		}
	}

	@Override
	public List<AccountSns> getAccountSnsList(GetAccountSnsArgs args) {
		try {
			ResponseEntity<BusinessResult<List<AccountSns>>> accountSnsMap = accountSnsClientApiFeignClient.getAccountSnsList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountSnsMap.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAccountSnsList error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> bindAccountSns(BindAccountSnsArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> accountSnsMap = accountSnsClientApiFeignClient.bindAccountSns(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountSnsMap.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("bindAccountSns error", e);
			throw e;
		}
	}

	@Override
	public Optional<String> unbindAccountSns(UnBindAccountSnsArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> accountSnsMap = accountSnsClientApiFeignClient.unbindAccountSns(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountSnsMap.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("unbindAccountSns error", e);
			throw e;
		}
	}
}
