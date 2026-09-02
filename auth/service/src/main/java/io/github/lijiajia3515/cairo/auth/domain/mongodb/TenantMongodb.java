package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
	 * 租户 mongodb模型
	 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMongodb {
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
	 * 名称
	 * 租户名称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantName;

	/**
	 * 别名
	 */
	@Field(write = Field.Write.ALWAYS)
	private String aliasName;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;

	/**
	 * 图标
	 * 图标资源地址
	 */
	@Field(write = Field.Write.ALWAYS)
	private String icon;

	/**
	 * 拥有者账号ID
	 * 所属账号的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String ownerAccountId;


	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AccountMetadataMongodb metadata = new AccountMetadataMongodb();

	public static MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAccountMetadataMongodbField {
		private MongodbField() {

		}

		public final String TENANT_ID = field("tenantId");
		public final String TENANT_NAME = field("tenantName");
		public final String ALIAS_NAME = field("aliasName");

		public final String OWNER_ACCOUNT_ID = field("ownerAccountId");

		public final String ICON = field("icon");

		public final String ENABLED = field("enabled");

	}
}
