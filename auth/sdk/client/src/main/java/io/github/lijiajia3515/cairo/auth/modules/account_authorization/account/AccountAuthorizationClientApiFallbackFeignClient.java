package io.github.lijiajia3515.cairo.auth.modules.account_authorization.account;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.AccountAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_authorization.GetAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

/**
 * client-api-account fallback feignclient
 */
public class AccountAuthorizationClientApiFallbackFeignClient implements AccountAuthorizationClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-账号子应用故障");


	@Override
	public ResponseEntity<BusinessResult<AccountAuthorizationModel>> getAccountAuthorization(String authorization, GetAccountAuthorizationArgs args) {
		throw EX;
	}
}
