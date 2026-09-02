package io.github.lijiajia3515.cairo.auth.modules.tenant_app_doc;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc.GetOnlineTenantAppDocArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * tenant app doc client api feign client
 */
@FeignClient(
	contextId = "tenantAppDocClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_doc",
	fallbackFactory = TenantAppDocClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantAppDocClientApiFeignClient {

	/**
	 * 获取预览文档token
	 * 需要权限: tenant_app_doc:preview
	 *
	 * @param args args 参数
	 * @return 在线文档地址
	 */
	@PostMapping("/get_preview_tenant_app_doc_token")
	ResponseEntity<BusinessResult<WebOfficeDocToken>> getPreviewTenantAppDocToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetOnlineTenantAppDocArgs args);

	/**
	 * 获取在线编辑文档url
	 * 需要权限：tenant_app_doc:edit
	 *
	 * @param args args
	 * @return 在线文档地址
	 */
	@PostMapping("/get_edit_tenant_app_doc_token")
	ResponseEntity<BusinessResult<WebOfficeDocToken>> getEditTenantAppDocToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			   @RequestBody GetOnlineTenantAppDocArgs args);
}
