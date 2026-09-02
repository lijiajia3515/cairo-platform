package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role_template;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.MetadataTenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;

import java.util.Map;
import java.util.Optional;


public class TenantAppRoleTemplateConverter {

	public static TenantAppRoleTemplate convert(TenantAppRoleTemplateMongodb m) {
		return TenantAppRoleTemplate.builder().tenantAppRoleTemplateId(m.getTenantAppRoleTemplateId())
			.enabled(m.getEnabled())
			.tenantAppRoleTemplateName(m.getTenantAppRoleTemplateName())
			.remark(m.getRemark())
			.build();
	}

	public static MetadataTenantAppRoleTemplate convert(TenantAppRoleTemplateMongodb m,
														Map<String, AppUser> metadataUserMap,
														Map<String, Integer> userCountMap,
														TenantAppRoleTemplateExtension extension) {
		final MetadataTenantAppRoleTemplate.MetadataTenantAppRoleTemplateBuilder<?, ?> builder = MetadataTenantAppRoleTemplate.builder()
			.tenantAppRoleTemplateId(m.getTenantAppRoleTemplateId())
			.enabled(m.getEnabled());
		if (extension.fields().contains(TenantAppRoleTemplateField.NAME)) {
			builder.tenantAppRoleTemplateName(m.getTenantAppRoleTemplateName());
		}
		Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppRoleTemplateField.USER_NUM))
			.map(x -> Optional.ofNullable(m.getTenantAppRoleTemplateId()).filter(userCountMap::containsKey).map(userCountMap::get).orElse(0))
			.ifPresent(builder::userNum);

		if (extension.fields().contains(TenantAppRoleTemplateField.REMARK)) {
			builder.remark(m.getRemark());
		}
		if (extension.fields().contains(TenantAppRoleTemplateField.METADATA)) {
			builder.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap));
		}
		return builder.build();
	}
}
