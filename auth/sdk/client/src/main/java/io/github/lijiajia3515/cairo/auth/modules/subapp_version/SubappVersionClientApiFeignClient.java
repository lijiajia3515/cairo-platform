package io.github.lijiajia3515.cairo.auth.modules.subapp_version;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_version.GetSubappVersionClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp_version.SubappVersion;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
	contextId = "subappVersionClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/subapp_version",
	fallbackFactory = SubappVersionClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface SubappVersionClientApiFeignClient {

	/**
	 * 获取子应用列表
	 *
	 * @param args 参数
	 * @return 子应用列表模式
	 */
	@PostMapping("/get_subapp_version_list")
	ResponseEntity<BusinessResult<List<SubappVersion>>> getSubappVersionList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetSubappVersionClientArgs args);

}
