package io.github.lijiajia3515.cairo.auth.modules.imgproxy;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.imgproxy.GetImgUrlArgs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client-api imgproxy feign client
 */
@FeignClient(
	contextId = "imgproxyClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/imgproxy",
	fallbackFactory = ImgproxyClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface ImgproxyClientApiFeignClient {
	// imgproxy
	@PostMapping("/get_proxy_url")
	ResponseEntity<BusinessResult<List<String>>> getProxyUrl(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody List<GetImgUrlArgs> params);

}
