package io.github.lijiajia3515.cairo.core.extension;

public enum DefaultField implements Field {
	NAME,
	METADATA;

	@Override
	public String field() {
		return name();
	}
}
