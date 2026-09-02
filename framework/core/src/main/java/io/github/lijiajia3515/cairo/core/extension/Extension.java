package io.github.lijiajia3515.cairo.core.extension;

import java.util.Set;

public interface Extension<T extends Field> {
	String name();

	Set<T> fields();
}
