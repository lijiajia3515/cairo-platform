package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
	contextId = "tenantAppUserTemplateClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_user_template",
	fallbackFactory = TenantAppUserTemplateClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface TenantAppUserTemplateClientApiFeignClient {

	/**
	 * 获取企业用户模板列表
	 *
	 * @return 企业用户模板列表
	 */
	@PostMapping("/get_tenant_app_user_template_list")
	ResponseEntity<BusinessResult<List<TenantAppUserTemplate>>> getTenantAppUserTemplateList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetTenantAppUserTemplateListArgs args);

}
