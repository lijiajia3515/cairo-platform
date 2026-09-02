package io.github.lijiajia3515.cairo.auth.modules.client;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client-api-account feignclient
 */
@FeignClient(
	contextId = "clientClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/client",
	fallbackFactory = ClientClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface ClientClientApiFeignClient {
	/**
	 * 获取客户端基础信息列表
	 *
	 * @param args 参数
	 * @return 客户端基础信息
	 */
	@PostMapping("/get_basic_client_list")
	ResponseEntity<BusinessResult<List<BasicClient>>> getBasicClientList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetClientArgs args);

	/**
	 * 获取客户端列表
	 *
	 * @param args 参数
	 * @return 客户端 列表模式
	 */
	@PostMapping("/get_client_list")
	ResponseEntity<BusinessResult<List<Client>>> getClientList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetClientArgs args);
}
