package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class TenantAppUserTemplateClientApiFallbackFeignClient implements TenantAppUserTemplateClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppUserTemplate>>> getTenantAppUserTemplateList(String authorization, GetTenantAppUserTemplateListArgs args) {
		throw EX;
	}
}
