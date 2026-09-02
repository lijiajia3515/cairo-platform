package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;


import java.util.List;

public interface TenantAppUserTemplateClientApiService {

	 List<TenantAppUserTemplate> getTenantAppUserTemplateList(GetTenantAppUserTemplateListArgs args);


}
