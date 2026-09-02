package io.github.lijiajia3515.cairo.auth.domain.dto.tenant;

import io.github.lijiajia3515.cairo.core.extension.Extension;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public enum TenantExtension implements Extension<TenantField>, Serializable {
	BASIC(TenantField.TENANT_NAME),
	ALL(TenantField.values());
	private final Set<TenantField> fields;

	TenantExtension(TenantField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toSet());
	}

	@Override
	public Set<TenantField> fields() {
		return fields;
	}
}
