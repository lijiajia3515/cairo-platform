package io.github.lijiajia3515.cairo.auth.domain.dto.tenant;

import io.github.lijiajia3515.cairo.core.extension.Field;

public enum TenantField implements Field {
	TENANT_NAME,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
