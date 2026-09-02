package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.core.page.Page;
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
	contextId = "appClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/app",
	fallbackFactory = AppClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface AppClientApiFeignClient {
	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_app_list")
	ResponseEntity<BusinessResult<List<App>>> getAppList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppArgs args);

	/**
	 * 获取app列表
	 *
	 * @param args 参数
	 * @return app 列表模式
	 */
	@PostMapping("/get_app_page_list")
	ResponseEntity<BusinessResult<Page<App>>> getAppPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetAppArgs args);
}
