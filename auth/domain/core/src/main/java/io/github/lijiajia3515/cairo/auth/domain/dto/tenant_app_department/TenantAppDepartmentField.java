package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department;

import io.github.lijiajia3515.cairo.core.extension.Field;

public enum TenantAppDepartmentField implements Field {
	NAME,
	PARENT_ID,
	REMARK,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
