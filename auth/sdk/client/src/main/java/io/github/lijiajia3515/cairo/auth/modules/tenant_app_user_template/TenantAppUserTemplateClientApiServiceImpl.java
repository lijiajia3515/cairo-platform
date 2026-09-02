package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantAppUserTemplateClientApiServiceImpl implements TenantAppUserTemplateClientApiService {

	private final TenantAppUserTemplateClientApiFeignClient tenantAppUserTemplateClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppUserTemplateClientApiServiceImpl(TenantAppUserTemplateClientApiFeignClient tenantAppUserTemplateClientApiFeignClient,
                                              CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppUserTemplateClientApiFeignClient = tenantAppUserTemplateClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public List<TenantAppUserTemplate> getTenantAppUserTemplateList(GetTenantAppUserTemplateListArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantAppUserTemplate>>> subappVersionList = tenantAppUserTemplateClientApiFeignClient.getTenantAppUserTemplateList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(subappVersionList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("subappVersion error", e);
			throw e;
		}
	}
}
