package io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

/**
	 * 企业应用级用户公众号链接
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WxmpTenantAppUserMongodb {

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
	 * 用户ID
	 * 所属用户的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userId;


	/**
	 * 微信ID
	 * 微信服务提供商标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String wxProviderId;

	/**
	 * 第三方认证唯一标识-openId
	 * 微信 OpenID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String openId;

	/**
	 * 绑定时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime bindTime;

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

		public final String USER_ID = field("userId");

		public final String WX_PROVIDER_ID = field("wxProviderId");
		public final String OPEN_ID = field("openId");

		public final String ENABLED = field("enabled");
	}
}
