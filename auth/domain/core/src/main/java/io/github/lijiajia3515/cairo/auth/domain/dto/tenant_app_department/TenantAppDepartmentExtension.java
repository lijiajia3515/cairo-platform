package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department;

import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public enum TenantAppDepartmentExtension implements Extension<TenantAppDepartmentField>, Serializable {
	BASIC(TenantAppDepartmentField.NAME, TenantAppDepartmentField.REMARK),
	INFO(TenantAppDepartmentField.NAME, TenantAppDepartmentField.REMARK, TenantAppDepartmentField.PARENT_ID),
	ALL(TenantAppDepartmentField.values());
	private final Set<TenantAppDepartmentField> fields;

	TenantAppDepartmentExtension(TenantAppDepartmentField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppDepartmentField> fields() {
		return fields;
	}
}
