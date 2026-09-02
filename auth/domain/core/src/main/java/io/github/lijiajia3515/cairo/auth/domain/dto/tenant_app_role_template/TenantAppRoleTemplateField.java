package io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template;

import io.github.lijiajia3515.cairo.core.extension.Field;

public enum TenantAppRoleTemplateField implements Field {
	NAME,
	REMARK,
	USER_NUM,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
