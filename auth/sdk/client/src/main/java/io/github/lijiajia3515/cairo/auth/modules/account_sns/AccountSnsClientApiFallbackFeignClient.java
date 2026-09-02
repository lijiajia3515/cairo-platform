package io.github.lijiajia3515.cairo.auth.modules.account_sns;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.AccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.UnBindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * client-api-user_connect fallback feignclient
 */
public class AccountSnsClientApiFallbackFeignClient implements AccountSnsClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<Map<String, SnsToken>>> getAccountSnsMap(String authorization, GetAccountSnsMapArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<AccountSns>>> getAccountSnsList(String authorization, GetAccountSnsArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> bindAccountSns(String authorization, BindAccountSnsArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Optional<String>>> unbindAccountSns(String authorization, UnBindAccountSnsArgs args) {
		throw EX;
	}
}
