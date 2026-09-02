package io.github.lijiajia3515.cairo.auth.modules.link;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.modules.HeaderConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;
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
	contextId = "linkClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/link",
	fallbackFactory = LinkClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface LinkClientApiFeignClient {

	/**
	 * 批量创建短链地址
	 * 需要权限：link:create_link
	 * @param args 参数
	 * @return 短链信息集合
	 */
	@PostMapping("/create_batch_link")
	ResponseEntity<BusinessResult<List<LinkInfo>>> createBatchLink(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																   @RequestHeader(HeaderConstants.TIMESTAMP_HEADER_NAME) String timestamp,
																   @RequestHeader(HeaderConstants.NONCE_HEADER_NAME) String nonce,
																   @RequestHeader(HeaderConstants.SIGN_HEADER_NAME) String sign,
																   @RequestBody CreateBatchLinkArgs args);

	/**
	 * 获取短链集合根据短链数组
	 * 需要权限：link:read
	 * @param args 参数
	 * @return 短链信息集合
	 */
	@PostMapping("/get_link_list_by_short_url")
	ResponseEntity<BusinessResult<List<LinkInfo>>> getLinkListByShortUrl(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetLinkListByShortUrlArgs args);

	/**
	 * 获取短链集合根据短链数组
	 * 需要权限：link:read
	 * @param args 参数
	 * @return 短链信息集合
	 */
	@PostMapping("/get_link_list_by_link_id")
	ResponseEntity<BusinessResult<List<LinkInfo>>> getLinkListByLinkId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetLinkListByLinkIdArgs args);

}
