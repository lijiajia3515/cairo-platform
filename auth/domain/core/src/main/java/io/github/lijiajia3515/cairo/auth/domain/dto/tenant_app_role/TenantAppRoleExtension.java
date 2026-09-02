package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum TenantAppRoleExtension implements Extension<TenantAppRoleField> {
	BASIC(TenantAppRoleField.NAME),
	INFO(TenantAppRoleField.NAME, TenantAppRoleField.REMARK),
	ALL(TenantAppRoleField.values());
	private final Set<TenantAppRoleField> fields;

	TenantAppRoleExtension(TenantAppRoleField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppRoleField> fields() {
		return fields;
	}
}
