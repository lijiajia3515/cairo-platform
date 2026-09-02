package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
	contextId = "endpointClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/endpoint",
	fallbackFactory = EndpointClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface EndpointClientApiFeignClient {

	/**
	 * 获取app终端列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_list")
	ResponseEntity<BusinessResult<List<Endpoint>>> getEndpointList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetEndpointClientArgs args);

	/**
	 * 获取app终端分页列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_page_list")
	ResponseEntity<BusinessResult<Page<Endpoint>>> getEndpointPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetEndpointClientArgs args);

	/**
	 * 获取app终端列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_endpoint_list_by_app")
	ResponseEntity<BusinessResult<List<Endpoint>>> getEndpointByAppList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetEndpointByAppClientArgs args);
}
