package io.github.lijiajia3515.cairo.mongodb.serial;

import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.NoneMetadataMongodb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor

@SuperBuilder(toBuilder = true)
public
class SerialMongodb {

	/**
	 * mongodb id
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * metadata数据
	 */
	@Builder.Default
	public NoneMetadataMongodb metadata = new NoneMetadataMongodb();

	/**
	 * 命名空间
	 */
	private String namespace;

	/**
	 * 用户key
	 */
	private String key;

	/**
	 * 值
	 */
	private Long value;

	/**
	 * 步长
	 */
	private Long step;

	public static final Field FIELD = new Field();

	/**
	 * mongodb字段
	 */
	public static class Field extends AbstractNoneMetadataMongodbField {
		public final String NAMESPACE = field("namespace");
		public final String KEY = field("key");
		public final String VALUE = field("value");

	}
}
