package io.github.lijiajia3515.cairo.auth.modules.app_doc.client;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_doc.GetPreviewAppDocTokenArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * app doc client api feign client
 */
@FeignClient(
	contextId = "appDocClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/app_doc",
	fallbackFactory = AppDocClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface AppDocClientApiFeignClient {


	/**
	 * 获取预览文档token
	 * 需要权限: app_doc:preview
	 *
	 * @param args args 参数
	 * @return 在线文档地址
	 */
	@PostMapping("/get_preview_app_doc_token")
	ResponseEntity<BusinessResult<WebOfficeDocToken>> getPreviewAppDocToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			@RequestBody GetPreviewAppDocTokenArgs args);

	/**
	 * 获取在线编辑文档url
	 * 需要权限：app_doc:edit
	 *
	 * @param args args
	 * @return 在线文档地址
	 */
	@PostMapping("/get_edit_app_doc_token")
	ResponseEntity<BusinessResult<WebOfficeDocToken>> getEditAppDocToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																		 @RequestBody GetPreviewAppDocTokenArgs args);
}
