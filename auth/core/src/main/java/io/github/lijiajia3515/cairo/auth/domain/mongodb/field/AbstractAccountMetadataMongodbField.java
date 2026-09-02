package io.github.lijiajia3515.cairo.auth.domain.mongodb.field;

import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;

/**
	 * 字段常量
	 */
public abstract class AbstractAccountMetadataMongodbField extends AbstractNoneMetadataMongodbField {
	public AbstractAccountMetadataMongodbField() {
		super();
	}

	public AbstractAccountMetadataMongodbField(AbstractMongodbField parent, String prefix) {
		super(parent, prefix);
	}

	public final Metadata METADATA = new Metadata(this, "metadata");

	public static class Metadata extends AbstractNoneMetadataMongodbField.Metadata {
		public Metadata() {
		}

		public Metadata(AbstractMongodbField parent, String prefix) {
			super(parent, prefix);
		}

		public final String CREATE_ACCOUNT_ID = field("createAccountId");
		public final String UPDATE_ACCOUNT_ID = field("updateAccountId");
	}
}
