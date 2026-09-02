package io.github.lijiajia3515.cairo.auth.modules.account_sns;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.AccountSns;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.BindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.GetAccountSnsMapArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns.UnBindAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * client api account connect feignclient
 */
@FeignClient(contextId = "accountSnsClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/account_sns",
	fallbackFactory = AccountSnsClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class)
public interface AccountSnsClientApiFeignClient {

	/**
	 * 获取账号的三方用户信息
	 * 需要权限： account_sns:read ｜account_sns:all
	 *
	 * @param args args
	 * @return 三方用户信息
	 */
	@PostMapping("/get_account_sns_map")
	ResponseEntity<BusinessResult<Map<String, SnsToken>>> getAccountSnsMap(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																		   @RequestBody GetAccountSnsMapArgs args);

	/**
	 * 查询账号三方绑定列表
	 * 需要权限： account_sns:read ｜account_sns:all
	 *
	 * @return AccountSns list
	 */
	@PostMapping("/get_account_sns_list")
	ResponseEntity<BusinessResult<List<AccountSns>>> getAccountSnsList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																	   @RequestBody GetAccountSnsArgs args);


	/**
	 * 绑定三方账号
	 * 需要权限： account_sns:bind ｜account_sns:all
	 *
	 * @param args args
	 */
	@PostMapping("/bind_account_sns")
	ResponseEntity<BusinessResult<Optional<String>>> bindAccountSns(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																	@RequestBody BindAccountSnsArgs args);


	/**
	 * 解绑三方账号
	 * 需要权限： account_sns:unbind ｜account_sns:all
	 *
	 * @param args args
	 */
	@PostMapping("/unbind_account_sns")
	ResponseEntity<BusinessResult<Optional<String>>> unbindAccountSns(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																	  @RequestBody UnBindAccountSnsArgs args);

}
