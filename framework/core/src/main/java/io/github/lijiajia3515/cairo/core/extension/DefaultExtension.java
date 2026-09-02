package io.github.lijiajia3515.cairo.core.extension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum DefaultExtension implements Extension<DefaultField> {

	BASIC(DefaultField.NAME),

	ALL(DefaultField.values());

	private final Set<DefaultField> fields;

	DefaultExtension(DefaultField... fields) {
		this.fields = Arrays.stream(fields).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<DefaultField> fields() {
		return fields;
	}
}
