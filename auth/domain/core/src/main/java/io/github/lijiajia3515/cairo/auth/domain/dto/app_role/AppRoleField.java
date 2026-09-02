package io.github.lijiajia3515.cairo.auth.domain.dto.app_role;

import io.github.lijiajia3515.cairo.core.extension.Field;

public enum AppRoleField implements Field {
	NAME,
	REMARK,
	USER_NUM,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
