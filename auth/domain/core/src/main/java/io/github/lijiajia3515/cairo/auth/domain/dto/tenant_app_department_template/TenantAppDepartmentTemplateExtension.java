package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template;

import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public enum TenantAppDepartmentTemplateExtension implements Extension<TenantAppDepartmentTemplateField>, Serializable {
	BASIC(TenantAppDepartmentTemplateField.NAME, TenantAppDepartmentTemplateField.REMARK),
	INFO(TenantAppDepartmentTemplateField.NAME, TenantAppDepartmentTemplateField.REMARK, TenantAppDepartmentTemplateField.PARENT_ID),
	ALL(TenantAppDepartmentTemplateField.values());
	private final Set<TenantAppDepartmentTemplateField> fields;

	TenantAppDepartmentTemplateExtension(TenantAppDepartmentTemplateField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantAppDepartmentTemplateField> fields() {
		return fields;
	}
}
