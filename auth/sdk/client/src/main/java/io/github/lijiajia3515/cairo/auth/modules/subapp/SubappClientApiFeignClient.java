package io.github.lijiajia3515.cairo.auth.modules.subapp;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
	contextId = "subappClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/subapp",
	fallbackFactory = SubappClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface SubappClientApiFeignClient {

	/**
	 * 获取子应用列表
	 *
	 * @param args 参数
	 * @return 子应用列表模式
	 */
	@PostMapping("/get_subapp_list")
	ResponseEntity<BusinessResult<List<Subapp>>> getSubappList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetSubappClientArgs args);

}
