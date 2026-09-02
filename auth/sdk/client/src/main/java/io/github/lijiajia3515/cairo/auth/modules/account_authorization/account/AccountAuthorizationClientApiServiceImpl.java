package io.github.lijiajia3515.cairo.auth.modules.account_authorization.account;


import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class AccountAuthorizationClientApiServiceImpl implements AccountAuthorizationClientApiService {

	private final AccountAuthorizationClientApiFeignClient accountAuthorizationClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public AccountAuthorizationClientApiServiceImpl(AccountAuthorizationClientApiFeignClient accountAuthorizationClientApiFeignClient,
													CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.accountAuthorizationClientApiFeignClient = accountAuthorizationClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public AccountAuthorizationModel getAccountAuthorization(GetAccountAuthorizationArgs args) {
		try {
			ResponseEntity<BusinessResult<AccountAuthorizationModel>> accountAuth = accountAuthorizationClientApiFeignClient.getAccountAuthorization(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(accountAuth.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getAccountAuth error", e);
			throw e;
		}
	}
}
