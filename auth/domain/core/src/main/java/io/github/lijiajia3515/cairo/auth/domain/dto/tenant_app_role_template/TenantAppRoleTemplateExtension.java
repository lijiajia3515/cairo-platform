package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TenantAppRoleTemplateExtension implements Extension<TenantAppRoleTemplateField> {
	BASIC(TenantAppRoleTemplateField.NAME),
	INFO(TenantAppRoleTemplateField.NAME, TenantAppRoleTemplateField.REMARK),
	ALL(TenantAppRoleTemplateField.values());
	private final Set<TenantAppRoleTemplateField> fields;

	TenantAppRoleTemplateExtension(TenantAppRoleTemplateField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppRoleTemplateField> fields() {
		return fields;
	}
}
