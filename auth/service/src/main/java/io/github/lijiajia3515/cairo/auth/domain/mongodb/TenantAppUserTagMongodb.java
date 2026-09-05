package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;

/**
	 * 企业应用级用户标签
	 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppUserTagMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 企业ID
	 * 所属企业的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantId;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;
	/**
	 * 标签ID
	 * 所属标签的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tagId;

	/**
	 * 账号标识
	 * 标签名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tagName;

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
	private TenantAppUserMetadataMongodb metadata = new TenantAppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");
		public final String TAG_ID = field("tagId");

		public final String TAG_NAME = field("tagName");

		public final String ENABLED = field("enabled");


	}
}
