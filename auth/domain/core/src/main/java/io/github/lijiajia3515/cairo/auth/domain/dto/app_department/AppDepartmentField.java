package io.github.lijiajia3515.cairo.auth.domain.dto.app_department;

import io.github.lijiajia3515.cairo.core.extension.Field;

public enum AppDepartmentField implements Field {
	NAME,
	PARENT_ID,
	REMARK,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
