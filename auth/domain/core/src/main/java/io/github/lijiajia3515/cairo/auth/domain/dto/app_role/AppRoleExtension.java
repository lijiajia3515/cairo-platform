package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;


import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AppRoleExtension implements Extension<AppRoleField> {
	BASIC(AppRoleField.NAME),
	INFO(AppRoleField.NAME, AppRoleField.REMARK),
	ALL(AppRoleField.values());
	private final Set<AppRoleField> fields;

	AppRoleExtension(AppRoleField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<AppRoleField> fields() {
		return fields;
	}
}
