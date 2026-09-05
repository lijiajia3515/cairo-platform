package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractTenantAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;


/**
	 * 企业应用级用户-社交登录
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppUserSnsMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 记录ID
	 * 记录唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String recordId;

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
	 * 用户ID
	 * 所属用户的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userId;


	/**
	 * 第三方认证提供商ID
	 * 所属第三方登录提供者的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String snsProviderId;

	/**
	 * UnionID
	 * 微信 UnionID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String unionId;

	/**
	 * 昵称
	 * 昵称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String nickname;

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

	public static class MongodbField extends AbstractTenantAppUserMetadataMongodbField {
		private MongodbField() {

		}

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String USER_ID = field("userId");

		public final String SNS_PROVIDER_ID = field("snsProviderId");
		public final String OPEN_ID = field("openId");

		public final String NICKNAME = field("nickname");

		public final String ENABLED = field("enabled");
	}
}
