package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
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
	 * app
	 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEndpointMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 租户ID
	 * 所属租户的唯一标识
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
	 * 终端ID
	 * 所属终端的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String endpointId;

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
	private AccountMetadataMongodb metadata = new AccountMetadataMongodb();

	/**
	 * 字段常量
	 */
	public static final MongodbField FIELD = new MongodbField();

	public static final class MongodbField extends AbstractAccountMetadataMongodbField {

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String ENDPOINT_ID = field("endpointId");

		public final String ENABLED = field("enabled");


	}
}
