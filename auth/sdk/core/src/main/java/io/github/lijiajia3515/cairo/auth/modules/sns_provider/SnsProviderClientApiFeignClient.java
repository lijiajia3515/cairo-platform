package io.github.lijiajia3515.cairo.auth.modules.sns_provider;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client api snsProvider feignclient
 */
@FeignClient(contextId = "snsProviderClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/sns_provider",
	fallbackFactory = SnsProviderClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class)
public interface SnsProviderClientApiFeignClient {

	/**
	 * 获取第三方认证提供方集合
	 *
	 * @param args 参数
	 * @return snsProvider list
	 */
	@PostMapping("/get_sns_provider_list")
	ResponseEntity<BusinessResult<List<SnsProvider>>> getSnsProviderList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetSnsProviderArgs args);


}
