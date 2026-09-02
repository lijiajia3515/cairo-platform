package io.github.lijiajia3515.cairo.mongodb.domain.field;

import io.github.lijiajia3515.cairo.mongodb.data.mapping.model.AbstractMongodbField;

/**
	 * 字段常量
	 */
public abstract class AbstractNoneMetadataMongodbField extends AbstractMongodbField {
	public AbstractNoneMetadataMongodbField() {
		super();
	}

	public AbstractNoneMetadataMongodbField(AbstractMongodbField parent, String prefix) {
		super(parent, prefix);
	}

	public final String _ID = field("_id");

	public final Metadata METADATA = new Metadata(this, "metadata");

	public static class Metadata extends AbstractMongodbField {
		public Metadata() {
		}

		public Metadata(AbstractMongodbField parent, String prefix) {
			super(parent, prefix);
		}
		public final String CREATE_TIME = field("createTime");
		public final String UPDATE_TIME = field("updateTime");

	}
}
