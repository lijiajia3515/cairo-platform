package io.github.lijiajia3515.cairo.auth.domain.mongodb.field;

import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;

/**
	 * 字段常量
	 */
public abstract class AbstractClientMetadataMongodbField extends AbstractNoneMetadataMongodbField {
	public AbstractClientMetadataMongodbField() {
		super();
	}

	public AbstractClientMetadataMongodbField(AbstractMongodbField parent, String prefix) {
		super(parent, prefix);
	}

	public final Metadata METADATA = new Metadata(this, "metadata");

	public static class Metadata extends AbstractNoneMetadataMongodbField.Metadata {
		public Metadata() {
		}

		public Metadata(AbstractMongodbField parent, String prefix) {
			super(parent, prefix);
		}

		public final String APP_ID = field("appId");
		public final String CREATE_CLIENT_ID = field("createClientId");
		public final String UPDATE_CLIENT_ID = field("updateClientId");
	}
}
