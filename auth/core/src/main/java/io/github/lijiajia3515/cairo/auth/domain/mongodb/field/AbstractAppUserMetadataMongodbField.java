package io.github.lijiajia3515.cairo.auth.domain.mongodb.field;

import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;

/**
	 * 字段常量
	 */
public abstract class AbstractAppUserMetadataMongodbField extends AbstractNoneMetadataMongodbField {
	public AbstractAppUserMetadataMongodbField() {
		super();
	}

	public AbstractAppUserMetadataMongodbField(AbstractMongodbField parent, String prefix) {
		super(parent, prefix);
	}

	public final Metadata METADATA = new Metadata(this, "metadata");

	public static class Metadata extends AbstractNoneMetadataMongodbField.Metadata {
		public Metadata() {
		}

		public Metadata(AbstractMongodbField parent, String prefix) {
			super(parent, prefix);
		}

		public final String CREATE_USER_ID = field("createUserId");
		public final String CREATE_TIME = field("createTime");
		public final String UPDATE_USER_ID = field("updateUserId");
		public final String UPDATE_TIME = field("updateTime");
	}
}
