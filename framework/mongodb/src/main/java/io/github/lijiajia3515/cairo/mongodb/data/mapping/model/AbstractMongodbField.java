package io.github.lijiajia3515.cairo.mongodb.data.mapping.model;

import java.util.Optional;

public abstract class AbstractMongodbField {
	public final AbstractMongodbField PARENT;
	public final String SELF;

	public AbstractMongodbField() {
		PARENT = null;
		SELF = null;
	}

	public AbstractMongodbField(AbstractMongodbField PARENT, String prefix) {
		this.PARENT = PARENT;
		this.SELF = Optional.ofNullable(PARENT.SELF).map(x -> x.concat(".")).orElse("").concat(prefix);
	}

	public String field(String fieldName) {
		return Optional.ofNullable(SELF).map(x -> x.concat(".").concat(fieldName)).orElse(fieldName);
	}

}
