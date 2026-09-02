package io.github.lijiajia3515.cairo.auth.domain.dto.app_department;

import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public enum AppDepartmentExtension implements Extension<AppDepartmentField>, Serializable {
	BASIC(AppDepartmentField.NAME, AppDepartmentField.REMARK),
	INFO(AppDepartmentField.NAME, AppDepartmentField.REMARK, AppDepartmentField.PARENT_ID),
	ALL(AppDepartmentField.values());
	private final Set<AppDepartmentField> fields;

	AppDepartmentExtension(AppDepartmentField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<AppDepartmentField> fields() {
		return fields;
	}
}
