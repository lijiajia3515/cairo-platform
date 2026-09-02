package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;

/**
	 * 子应用版本
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubappVersionMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	@Field(write = Field.Write.ALWAYS)
	private ObjectId _id;

	/**
	 * 子应用ID
	 * 所属子应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappId;

	/**
	 * 子应用版本
	 * 所属子应用的版本号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappVersion;

	/**
	 * 子应用备注
	 */
	@Field(write = Field.Write.ALWAYS)
	private String subappRemark;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

	/**
	 * 字段常量
	 */
	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String SUBAPP_ID = field("subappId");
		public final String SUBAPP_VERSION = field("subappVersion");
		public final String SUBAPP_REMARK = field("subappRemark");
		public final String ENABLED = field("enabled");
	}
}
