package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Set;

@JsonDeserialize(using = EmptySetDeserializer.class)
public class EmptySetMixin {

	@JsonCreator
	EmptySetMixin(Set<?> set) {

	}
}
